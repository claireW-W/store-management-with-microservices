package com.store.store.service;

import com.store.store.dto.*;
import com.store.store.model.*;
import com.store.store.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Order Service - handles order business logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final ProductRepository productRepository;
    private final com.store.store.repository.UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final com.store.store.messaging.MessagePublisher messagePublisher;
    
    @Value("${services.warehouse.url:http://localhost:8083/api}")
    private String warehouseServiceUrl;
    
    @Value("${services.bank.url:http://localhost:8082/api}")
    private String bankServiceUrl;
    
    @Value("${services.delivery.url:http://localhost:8084/api}")
    private String deliveryServiceUrl;
    
    @Value("${services.email.url:http://localhost:8085/api}")
    private String emailServiceUrl;

    @Value("${store.payment.simulate-on-failure:true}")
    private boolean simulatePaymentOnFailure;
    
    /**
     * Create a new order
     * POST /api/orders
     */
    @Transactional
    public OrderResponse createOrder(Long userId, CreateOrderRequest request) {
        String requestId = UUID.randomUUID().toString();
        log.info("[{}] Creating order for user: {}", requestId, userId);
        
        // 1. Validate products and calculate total
        List<Product> products = validateAndGetProducts(request.getItems());
        BigDecimal totalAmount = calculateTotalAmount(request.getItems(), products);
        
        // 2. Create order
        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .userId(userId)
                .status("PENDING")
                .totalAmount(totalAmount)
                .shippingAddress(convertAddressToMap(request.getShippingAddress()))
                .billingAddress(request.getBillingAddress() != null ? 
                        convertAddressToMap(request.getBillingAddress()) : null)
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus("PENDING")
                .notes(request.getNotes())
                .build();
        
        order = orderRepository.save(order);
        log.info("[{}] Order created with order number: {}", requestId, order.getOrderNumber());
        
        // 3. Create order items
        List<OrderItem> orderItems = createOrderItems(order.getId(), request.getItems(), products);
        orderItemRepository.saveAll(orderItems);
        
        // 4. Create order status history
        createOrderStatusHistory(order.getId(), "PENDING", "Order created");
        
        // 5. Check inventory (call Warehouse Service)
        try {
            checkInventory(requestId, request.getItems());
        } catch (Exception e) {
            log.error("[{}] Inventory check failed: {}", requestId, e.getMessage());
            // Send inventory insufficient notification email
            try {
                sendInventoryInsufficientEmail(requestId, userId, order.getOrderNumber(), e.getMessage());
            } catch (Exception emailErr) {
                log.error("[{}] Failed to send inventory insufficient email: {}", requestId, emailErr.getMessage());
            }
            throw new IllegalStateException("Inventory check failed: " + e.getMessage());
        }
        
        // 6. Reserve inventory (call Warehouse Service)
        try {
            reserveInventory(requestId, order.getOrderNumber(), request.getItems());
        } catch (Exception e) {
            log.error("[{}] Inventory reservation failed: {}", requestId, e.getMessage());
            // Send inventory insufficient notification email
            try {
                sendInventoryInsufficientEmail(requestId, userId, order.getOrderNumber(), e.getMessage());
            } catch (Exception emailErr) {
                log.error("[{}] Failed to send inventory insufficient email: {}", requestId, emailErr.getMessage());
            }
            throw new IllegalStateException("Inventory reservation failed: " + e.getMessage());
        }
        
        // 7. Process payment (call Bank Service)
        // Note: This is simplified. In real scenario, you'd get customer account from user info
        try {
            String transactionId = processPayment(requestId, order, userId);
            order.setPaymentTransactionId(transactionId);
            order.setPaymentStatus("PAID");
            order.setStatus("PAID");
            orderRepository.save(order);
            createOrderStatusHistory(order.getId(), "PAID", "Payment successful");
            log.info("[{}] Payment successful: {}", requestId, transactionId);
        } catch (Exception e) {
            log.error("[{}] Payment failed: {}", requestId, e.getMessage());
            if (simulatePaymentOnFailure) {
                String simulatedTxn = "SIM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                order.setPaymentTransactionId(simulatedTxn);
                order.setPaymentStatus("PAID");
                order.setStatus("PAID");
                orderRepository.save(order);
                createOrderStatusHistory(order.getId(), "PAID", "Payment simulated due to gateway failure");
                log.warn("[{}] Payment gateway failed, simulated payment applied: {}", requestId, simulatedTxn);
            } else {
                order.setPaymentStatus("FAILED");
                orderRepository.save(order);
                // Send payment failure notification email (e.g., insufficient balance)
                try {
                    sendPaymentFailureEmail(requestId, userId, order.getOrderNumber(), order.getTotalAmount(), e.getMessage());
                } catch (Exception emailErr) {
                    log.error("[{}] Failed to send payment failure email: {}", requestId, emailErr.getMessage());
                }
                throw new IllegalStateException("Payment failed: " + e.getMessage());
            }
        }
        
        // 8. Create delivery request (call Delivery Service)
        try {
            String deliveryId = createDeliveryRequest(requestId, order);
            order.setDeliveryId(deliveryId);
            order.setStatus("PROCESSING");
            orderRepository.save(order);
            createOrderStatusHistory(order.getId(), "PROCESSING", "Delivery created");
            log.info("[{}] Delivery created: {}", requestId, deliveryId);
        } catch (Exception e) {
            log.error("[{}] Delivery creation failed: {}", requestId, e.getMessage());
            // Don't fail the order, just log the error
        }
        
        // 9. Send order confirmation email (call Email Service)
        try {
            sendOrderConfirmationEmail(requestId, order);
        } catch (Exception e) {
            log.error("[{}] Failed to send order confirmation email: {}", requestId, e.getMessage());
            // Don't fail the order, just log the error
        }
        
        return convertToOrderResponse(order, orderItems, products);
    }
    
    /**
     * Get all orders for a user
     * GET /api/orders
     */
    public List<OrderResponse> getOrders(Long userId) {
        log.info("Getting orders for user: {}", userId);
        
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        
        return orders.stream()
                .map(order -> {
                    List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
                    List<Long> productIds = items.stream()
                            .map(OrderItem::getProductId)
                            .collect(Collectors.toList());
                    List<Product> products = productRepository.findAllById(productIds);
                    return convertToOrderResponse(order, items, products);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Get a specific order by ID for a user
     * GET /api/orders/{id}
     */
    public OrderResponse getOrderById(Long userId, Long orderId) {
        log.info("Getting order {} for user: {}", orderId, userId);
        
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new IllegalArgumentException("Order not found");
        }
        
        Order order = orderOpt.get();
        
        // Check if the order belongs to the user
        if (!order.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Order not found");
        }
        
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        List<Long> productIds = items.stream()
                .map(OrderItem::getProductId)
                .collect(Collectors.toList());
        List<Product> products = productRepository.findAllById(productIds);
        
        return convertToOrderResponse(order, items, products);
    }
    
    /**
     * Cancel an order
     * POST /api/orders/{orderId}/cancel
     */
    @Transactional
    public CancelOrderResponse cancelOrder(Long userId, Long orderId, CancelOrderRequest request) {
        String requestId = UUID.randomUUID().toString();
        log.info("[{}] Canceling order: {} for user: {}", requestId, orderId, userId);
        
        // 1. Find and validate order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        
        if (!order.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Order does not belong to this user");
        }
        
        if ("CANCELLED".equals(order.getStatus()) || "REFUNDED".equals(order.getStatus())) {
            throw new IllegalStateException("Order is already cancelled or refunded");
        }
        
        if ("DELIVERED".equals(order.getStatus())) {
            throw new IllegalStateException("Delivered orders cannot be cancelled");
        }
        
        // 2. Update order status
        order.setStatus("CANCELLED");
        orderRepository.save(order);
        createOrderStatusHistory(order.getId(), "CANCELLED", 
                request.getReason() != null ? request.getReason() : "Cancelled by user");

        // Publish cancel events to downstream (Warehouse already handled via existing publisher; notify Delivery too)
        try {
            messagePublisher.publishOrderCancelledEvent(order, request.getReason() != null ? request.getReason() : "Cancelled by user");
        } catch (Exception ignore) { }
        try {
            messagePublisher.publishOrderCancelledToDelivery(order);
        } catch (Exception ignore) { }
        
        // 3. Process refund if payment was made (call Bank Service)
        String refundTransactionId = null;
        boolean refundProcessed = false;
        
        if ("PAID".equals(order.getPaymentStatus()) && order.getPaymentTransactionId() != null) {
            try {
                refundTransactionId = processRefund(requestId, order);
                order.setPaymentStatus("REFUNDED");
                orderRepository.save(order);
                refundProcessed = true;
                log.info("[{}] Refund successful: {}", requestId, refundTransactionId);
                
                // Send refund notification email
                try {
                    sendRefundNotificationEmail(requestId, order);
                } catch (Exception e) {
                    log.error("[{}] Failed to send refund notification email: {}", requestId, e.getMessage());
                    // Don't fail cancellation if email fails
                }
            } catch (Exception e) {
                log.error("[{}] Refund failed: {}", requestId, e.getMessage());
            }
        } else {
            // Send cancellation email even if no refund (order was not paid yet)
            try {
                sendCancellationEmail(requestId, order);
            } catch (Exception e) {
                log.error("[{}] Failed to send cancellation email: {}", requestId, e.getMessage());
                // Don't fail cancellation if email fails
            }
        }
        
        return CancelOrderResponse.builder()
                .message("Order cancelled successfully")
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus())
                .refundTransactionId(refundTransactionId)
                .refundProcessed(refundProcessed)
                .build();
    }
    
    // ========== Private Helper Methods ==========
    
    private List<Product> validateAndGetProducts(List<OrderItemDTO> items) {
        List<Long> productIds = items.stream()
                .map(OrderItemDTO::getProductId)
                .collect(Collectors.toList());
        
        List<Product> products = productRepository.findAllById(productIds);
        
        if (products.size() != productIds.size()) {
            throw new IllegalArgumentException("Some products not found");
        }
        
        // Check if all products are active
        products.forEach(product -> {
            if (!product.getIsActive()) {
                throw new IllegalArgumentException("Product is not active: " + product.getName());
            }
        });
        
        return products;
    }
    
    private BigDecimal calculateTotalAmount(List<OrderItemDTO> items, List<Product> products) {
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));
        
        return items.stream()
                .map(item -> {
                    Product product = productMap.get(item.getProductId());
                    return product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private List<OrderItem> createOrderItems(Long orderId, List<OrderItemDTO> itemDTOs, 
                                             List<Product> products) {
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));
        
        return itemDTOs.stream()
                .map(dto -> {
                    Product product = productMap.get(dto.getProductId());
                    BigDecimal totalPrice = product.getPrice()
                            .multiply(BigDecimal.valueOf(dto.getQuantity()));
                    
                    return OrderItem.builder()
                            .orderId(orderId)
                            .productId(dto.getProductId())
                            .quantity(dto.getQuantity())
                            .unitPrice(product.getPrice())
                            .totalPrice(totalPrice)
                            .build();
                })
                .collect(Collectors.toList());
    }
    
    private void createOrderStatusHistory(Long orderId, String status, String notes) {
        OrderStatusHistory history = OrderStatusHistory.builder()
                .orderId(orderId)
                .status(status)
                .notes(notes)
                .build();
        orderStatusHistoryRepository.save(history);
    }
    
    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", new Random().nextInt(10000));
        return "ORD-" + timestamp + "-" + random;
    }
    
    private Map<String, Object> convertAddressToMap(AddressDTO address) {
        Map<String, Object> map = new HashMap<>();
        map.put("street", address.getStreet());
        map.put("suburb", address.getSuburb());
        map.put("state", address.getState());
        map.put("postcode", address.getPostcode());
        map.put("country", address.getCountry());
        // Provide a fullAddress field for downstream services (Delivery) that expect a single string
        String fullAddress = String.format("%s, %s, %s %s, %s",
                safe(address.getStreet()),
                safe(address.getSuburb()),
                safe(address.getState()),
                safe(address.getPostcode()),
                safe(address.getCountry()));
        map.put("fullAddress", fullAddress.trim().replaceAll(", ", ", ").replaceAll("^, ", ""));
        return map;
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
    
    private OrderResponse convertToOrderResponse(Order order, List<OrderItem> items, 
                                                 List<Product> products) {
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));
        
        List<OrderItemDTO> itemDTOs = items.stream()
                .map(item -> {
                    Product product = productMap.get(item.getProductId());
                    return OrderItemDTO.builder()
                            .productId(item.getProductId())
                            .quantity(item.getQuantity())
                            .unitPrice(item.getUnitPrice())
                            .totalPrice(item.getTotalPrice())
                            .productName(product != null ? product.getName() : null)
                            .productSku(product != null ? product.getSku() : null)
                            .productDescription(product != null ? product.getDescription() : null)
                            .productWeight(product != null ? product.getWeight() : null)
                            .build();
                })
                .collect(Collectors.toList());
        
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUserId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
                .billingAddress(order.getBillingAddress())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .paymentTransactionId(order.getPaymentTransactionId())
                .deliveryId(order.getDeliveryId())
                .notes(order.getNotes())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(itemDTOs)
                .build();
    }
    
    // ========== External Service Calls ==========
    
    private void checkInventory(String requestId, List<OrderItemDTO> items) {
        log.info("[{}] Checking inventory for {} items", requestId, items.size());
        
        for (OrderItemDTO item : items) {
            try {
                String url = warehouseServiceUrl + "/warehouse/inventory/" + item.getProductId();
                ResponseEntity<Object[]> response = restTemplate.getForEntity(url, Object[].class);
                
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    log.info("[{}] Inventory check successful for product: {}", requestId, item.getProductId());
                } else {
                    throw new IllegalStateException("No inventory available for product: " + item.getProductId());
                }
            } catch (Exception e) {
                log.error("[{}] Inventory check failed for product: {} - {}", requestId, item.getProductId(), e.getMessage());
                throw new IllegalStateException("Inventory check failed for product: " + item.getProductId());
            }
        }
    }
    
    private void reserveInventory(String requestId, String orderNumber, List<OrderItemDTO> items) {
        log.info("[{}] Reserving inventory for order: {}", requestId, orderNumber);
        
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("orderId", orderNumber);
            requestBody.put("items", items.stream().map(item -> {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("productId", item.getProductId());
                itemMap.put("quantity", item.getQuantity());
                return itemMap;
            }).collect(Collectors.toList()));
            
            String url = warehouseServiceUrl + "/warehouse/reserve";
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestBody, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("[{}] Inventory reservation successful for order: {}", requestId, orderNumber);
            } else {
                throw new IllegalStateException("Inventory reservation failed");
            }
        } catch (Exception e) {
            log.error("[{}] Inventory reservation failed for order: {} - {}", requestId, orderNumber, e.getMessage());
            throw new IllegalStateException("Inventory reservation failed: " + e.getMessage());
        }
    }
    
    private String processPayment(String requestId, Order order, Long userId) {
        log.info("[{}] Processing payment for order: {}", requestId, order.getOrderNumber());
        
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("orderId", order.getOrderNumber());
            requestBody.put("customerId", "CUST-" + userId); // Simplified customer ID
            requestBody.put("amount", order.getTotalAmount());
            requestBody.put("currency", "AUD");
            requestBody.put("paymentMethod", order.getPaymentMethod());
            requestBody.put("description", "Order payment for " + order.getOrderNumber());
            
            String url = bankServiceUrl + "/bank/payment";
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestBody, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                String transactionId = (String) responseBody.get("transactionId");
                log.info("[{}] Payment successful: {}", requestId, transactionId);
                return transactionId;
            } else {
                throw new IllegalStateException("Payment processing failed");
            }
        } catch (Exception e) {
            log.error("[{}] Payment processing failed for order: {} - {}", requestId, order.getOrderNumber(), e.getMessage());
            throw new IllegalStateException("Payment processing failed: " + e.getMessage());
        }
    }
    
    private String processRefund(String requestId, Order order) {
        log.info("[{}] Processing refund for order: {}", requestId, order.getOrderNumber());
        
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("transactionId", order.getPaymentTransactionId());
            requestBody.put("orderId", order.getOrderNumber());
            requestBody.put("refundAmount", order.getTotalAmount());
            requestBody.put("reason", "Order cancellation");
            requestBody.put("description", "Full refund for cancelled order " + order.getOrderNumber());
            
            String url = bankServiceUrl + "/bank/refund";
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestBody, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                String refundId = (String) responseBody.get("refundId");
                log.info("[{}] Refund successful: {}", requestId, refundId);
                return refundId;
            } else {
                throw new IllegalStateException("Refund processing failed");
            }
        } catch (Exception e) {
            log.error("[{}] Refund processing failed for order: {} - {}", requestId, order.getOrderNumber(), e.getMessage());
            throw new IllegalStateException("Refund processing failed: " + e.getMessage());
        }
    }
    
    private String createDeliveryRequest(String requestId, Order order) {
        log.info("[{}] Creating delivery request for order: {}", requestId, order.getOrderNumber());
        
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("orderId", order.getOrderNumber());
            requestBody.put("customerId", "CUST-" + order.getUserId());
            requestBody.put("shippingAddress", order.getShippingAddress().get("fullAddress"));
            requestBody.put("warehouseId", 1); // Default warehouse
            requestBody.put("carrier", "Australia Post");
            requestBody.put("notes", "Order from Store Backend");
            
            String url = deliveryServiceUrl + "/delivery/request";
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestBody, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                String deliveryId = (String) responseBody.get("deliveryId");
                log.info("[{}] Delivery request created: {}", requestId, deliveryId);
                return deliveryId;
            } else {
                throw new IllegalStateException("Delivery request creation failed");
            }
        } catch (Exception e) {
            log.error("[{}] Delivery request creation failed for order: {} - {}", requestId, order.getOrderNumber(), e.getMessage());
            throw new IllegalStateException("Delivery request creation failed: " + e.getMessage());
        }
    }
    
    private void sendOrderConfirmationEmail(String requestId, Order order) {
        log.info("[{}] Sending order confirmation email for order: {}", requestId, order.getOrderNumber());
        
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("recipientEmail", "customer@example.com"); // Default email
            requestBody.put("subject", "Order Confirmation - " + order.getOrderNumber());
            requestBody.put("orderId", order.getOrderNumber());
            requestBody.put("orderNumber", order.getOrderNumber());
            requestBody.put("totalAmount", order.getTotalAmount());
            requestBody.put("status", "CONFIRMED");
            requestBody.put("message", "Your order has been confirmed and is being processed.");
            
            String url = emailServiceUrl + "/email/order-confirmation";
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestBody, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("[{}] Order confirmation email sent successfully", requestId);
            } else {
                log.warn("[{}] Failed to send order confirmation email", requestId);
            }
        } catch (Exception e) {
            log.error("[{}] Failed to send order confirmation email: {}", requestId, e.getMessage());
        }
    }
    
    private void sendRefundNotificationEmail(String requestId, Order order) {
        log.info("[{}] Sending refund notification email for order: {}", requestId, order.getOrderNumber());
        
        try {
            // Get user email from User entity
            Optional<com.store.store.model.User> userOpt = userRepository.findById(order.getUserId());
            String userEmail = userOpt.map(com.store.store.model.User::getEmail)
                    .orElse("customer@example.com"); // Fallback to default if user not found
            String userName = userOpt.map(u -> u.getFirstName() + " " + u.getLastName())
                    .orElse("Customer"); // Use user's name if available
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("recipientEmail", userEmail); // Use actual user email
            requestBody.put("recipientName", userName); // Use user's name
            requestBody.put("subject", "Refund Processed - " + order.getOrderNumber());
            requestBody.put("orderId", order.getOrderNumber());
            requestBody.put("orderNumber", order.getOrderNumber());
            requestBody.put("refundAmount", order.getTotalAmount());
            requestBody.put("amount", order.getTotalAmount().toString()); // For email service
            requestBody.put("status", "REFUNDED");
            requestBody.put("message", "Your refund has been processed successfully.");
            requestBody.put("content", "Your refund for order " + order.getOrderNumber() + 
                    " has been processed. Refund Amount: " + order.getTotalAmount());
            
            String url = emailServiceUrl + "/email/refund-notification";
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestBody, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("[{}] Refund notification email sent successfully to: {}", requestId, userEmail);
            } else {
                log.warn("[{}] Failed to send refund notification email to: {}", requestId, userEmail);
            }
        } catch (Exception e) {
            log.error("[{}] Failed to send refund notification email for order {}: {}", 
                    requestId, order.getOrderNumber(), e.getMessage(), e);
        }
    }
    
    private void sendCancellationEmail(String requestId, Order order) {
        log.info("[{}] Sending cancellation email for order: {}", requestId, order.getOrderNumber());
        
        try {
            // Get user email from User entity
            Optional<com.store.store.model.User> userOpt = userRepository.findById(order.getUserId());
            String userEmail = userOpt.map(com.store.store.model.User::getEmail)
                    .orElse("customer@example.com"); // Fallback to default if user not found
            String userName = userOpt.map(u -> u.getFirstName() + " " + u.getLastName())
                    .orElse("Customer"); // Use user's name if available
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("recipientEmail", userEmail); // Use actual user email
            requestBody.put("recipientName", userName); // Use user's name
            requestBody.put("subject", "Order Cancelled - " + order.getOrderNumber());
            requestBody.put("orderId", order.getOrderNumber());
            requestBody.put("orderNumber", order.getOrderNumber());
            requestBody.put("status", "CANCELLED");
            requestBody.put("message", "Your order has been cancelled successfully.");
            requestBody.put("content", "Your order " + order.getOrderNumber() + " has been cancelled.");
            
            // Use refund-notification endpoint as it handles cancellation cases too
            String url = emailServiceUrl + "/email/refund-notification";
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestBody, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("[{}] Cancellation email sent successfully to: {}", requestId, userEmail);
            } else {
                log.warn("[{}] Failed to send cancellation email to: {}", requestId, userEmail);
            }
        } catch (Exception e) {
            log.error("[{}] Failed to send cancellation email for order {}: {}", 
                    requestId, order.getOrderNumber(), e.getMessage(), e);
        }
    }
    
    private void sendPaymentFailureEmail(String requestId, Long userId, String orderNumber, 
                                         BigDecimal orderAmount, String errorMessage) {
        log.info("[{}] Sending payment failure notification email for order: {}", requestId, orderNumber);
        
        try {
            // Get user email from User entity
            Optional<com.store.store.model.User> userOpt = userRepository.findById(userId);
            String userEmail = userOpt.map(com.store.store.model.User::getEmail)
                    .orElse("customer@example.com");
            String userName = userOpt.map(u -> u.getFirstName() + " " + u.getLastName())
                    .orElse("Customer");
            
            // Check if error is due to insufficient balance
            boolean isInsufficientBalance = errorMessage != null && 
                    errorMessage.toLowerCase().contains("insufficient");
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("recipientEmail", userEmail);
            requestBody.put("recipientName", userName);
            requestBody.put("subject", isInsufficientBalance ? 
                    "Payment Failed - Insufficient Balance - " + orderNumber : 
                    "Payment Failed - " + orderNumber);
            requestBody.put("orderId", orderNumber);
            requestBody.put("orderNumber", orderNumber);
            requestBody.put("status", "PAYMENT_FAILED");
            requestBody.put("message", isInsufficientBalance ? 
                    "Your payment failed due to insufficient account balance. Order amount: " + orderAmount + 
                    ". Please add funds to your account and try again." :
                    "Your payment failed: " + errorMessage + ". Please try again or contact support.");
            requestBody.put("content", isInsufficientBalance ?
                    "Your order " + orderNumber + " could not be processed due to insufficient balance. " +
                    "Order Total: $" + orderAmount + ". Please add funds to your account and try again." :
                    "Your order " + orderNumber + " payment failed: " + errorMessage + ". Please try again.");
            
            // Use refund-notification endpoint as it handles various failure cases
            String url = emailServiceUrl + "/email/refund-notification";
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestBody, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("[{}] Payment failure notification email sent successfully to: {}", requestId, userEmail);
            } else {
                log.warn("[{}] Failed to send payment failure notification email to: {}", requestId, userEmail);
            }
        } catch (Exception e) {
            log.error("[{}] Failed to send payment failure notification email for order {}: {}", 
                    requestId, orderNumber, e.getMessage(), e);
        }
    }
    
    private void sendInventoryInsufficientEmail(String requestId, Long userId, String orderNumber, String errorMessage) {
        log.info("[{}] Sending inventory insufficient notification email for order: {}", requestId, orderNumber);
        
        try {
            // Get user email from User entity
            Optional<com.store.store.model.User> userOpt = userRepository.findById(userId);
            String userEmail = userOpt.map(com.store.store.model.User::getEmail)
                    .orElse("customer@example.com");
            String userName = userOpt.map(u -> u.getFirstName() + " " + u.getLastName())
                    .orElse("Customer");
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("recipientEmail", userEmail);
            requestBody.put("recipientName", userName);
            requestBody.put("subject", "Inventory Unavailable - Order " + orderNumber);
            requestBody.put("orderId", orderNumber);
            requestBody.put("orderNumber", orderNumber);
            requestBody.put("status", "INVENTORY_INSUFFICIENT");
            
            // Extract available quantity if mentioned in error message
            String availableQty = "";
            if (errorMessage != null && errorMessage.contains("Available:")) {
                // Try to extract available quantity from error message
                // e.g., "Insufficient inventory. Available: 5, Required: 6"
            }
            
            requestBody.put("message", "Your order could not be processed due to insufficient inventory. " + 
                    (errorMessage != null ? errorMessage : "Please try ordering a smaller quantity."));
            requestBody.put("content", "Your order " + orderNumber + " could not be processed due to insufficient inventory. " +
                    (errorMessage != null ? errorMessage : "Please try ordering a smaller quantity or check back later."));
            
            // Use refund-notification endpoint
            String url = emailServiceUrl + "/email/refund-notification";
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestBody, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("[{}] Inventory insufficient notification email sent successfully to: {}", requestId, userEmail);
            } else {
                log.warn("[{}] Failed to send inventory insufficient notification email to: {}", requestId, userEmail);
            }
        } catch (Exception e) {
            log.error("[{}] Failed to send inventory insufficient notification email for order {}: {}", 
                    requestId, orderNumber, e.getMessage(), e);
        }
    }
}


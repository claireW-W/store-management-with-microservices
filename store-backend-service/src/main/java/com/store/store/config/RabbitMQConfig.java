package com.store.store.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * RabbitMQ Configuration for Store Backend Service
 * Defines exchanges, queues, and bindings for order-related messaging
 */
@Configuration
public class RabbitMQConfig {
    
    // ========== Exchanges ==========
    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String BANK_EXCHANGE = "bank.exchange";
    public static final String WAREHOUSE_EXCHANGE = "warehouse.exchange";
    public static final String DELIVERY_EXCHANGE = "delivery.exchange";
    
    // ========== Queues ==========
    // Store Backend queues for receiving messages
    public static final String PAYMENT_RESULT_QUEUE = "store.payment.result.queue";
    public static final String WAREHOUSE_RESULT_QUEUE = "store.warehouse.result.queue";
    public static final String DELIVERY_STATUS_QUEUE = "store.delivery.status.queue";
    
    // ========== Routing Keys ==========
    // Bank Service routing keys (receive)
    public static final String BANK_PAYMENT_SUCCESS_KEY = "bank.payment.success";
    public static final String BANK_PAYMENT_FAILED_KEY = "bank.payment.failure";
    public static final String BANK_REFUND_SUCCESS_KEY = "bank.refund.success";
    
    // Warehouse Service routing keys (receive)
    public static final String WAREHOUSE_STOCK_RESERVED_KEY = "warehouse.stock.reserved";
    public static final String WAREHOUSE_STOCK_INSUFFICIENT_KEY = "warehouse.stock.insufficient";
    public static final String WAREHOUSE_STOCK_DEDUCTED_KEY = "warehouse.stock.deducted";
    
    // Delivery Service routing keys (receive)
    public static final String DELIVERY_CREATED_KEY = "delivery.created";
    public static final String DELIVERY_SHIPPED_KEY = "delivery.shipped";
    public static final String DELIVERY_IN_TRANSIT_KEY = "delivery.in-transit";
    public static final String DELIVERY_DELIVERED_KEY = "delivery.delivered";
    public static final String DELIVERY_LOST_KEY = "delivery.lost";
    public static final String DELIVERY_DELAYED_KEY = "delivery.delayed";
    
    // Store Backend routing keys (send)
    public static final String ORDER_CREATED_KEY = "order.created";
    public static final String ORDER_PAID_KEY = "order.paid";
    public static final String ORDER_CANCELLED_KEY = "order.cancelled";
    public static final String ORDER_COMPLETED_KEY = "order.completed";
    
    // ========== Exchange Definitions ==========
    
    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE);
    }
    
    @Bean
    public TopicExchange bankExchange() {
        return new TopicExchange(BANK_EXCHANGE);
    }
    
    @Bean
    public TopicExchange warehouseExchange() {
        return new TopicExchange(WAREHOUSE_EXCHANGE);
    }
    
    @Bean
    public TopicExchange deliveryExchange() {
        return new TopicExchange(DELIVERY_EXCHANGE);
    }
    
    // ========== Queue Definitions ==========
    
    @Bean
    public Queue paymentResultQueue() {
        return new Queue(PAYMENT_RESULT_QUEUE, true); // Durable queue
    }
    
    @Bean
    public Queue warehouseResultQueue() {
        return new Queue(WAREHOUSE_RESULT_QUEUE, true);
    }
    
    @Bean
    public Queue deliveryStatusQueue() {
        return new Queue(DELIVERY_STATUS_QUEUE, true);
    }
    
    // ========== Bindings ==========
    
    // Bind to Bank Exchange - receive payment results
    @Bean
    public Binding paymentSuccessBinding(Queue paymentResultQueue, TopicExchange bankExchange) {
        return BindingBuilder.bind(paymentResultQueue)
                .to(bankExchange)
                .with(BANK_PAYMENT_SUCCESS_KEY);
    }
    
    @Bean
    public Binding paymentFailedBinding(Queue paymentResultQueue, TopicExchange bankExchange) {
        return BindingBuilder.bind(paymentResultQueue)
                .to(bankExchange)
                .with(BANK_PAYMENT_FAILED_KEY);
    }
    
    @Bean
    public Binding refundSuccessBinding(Queue paymentResultQueue, TopicExchange bankExchange) {
        return BindingBuilder.bind(paymentResultQueue)
                .to(bankExchange)
                .with(BANK_REFUND_SUCCESS_KEY);
    }
    
    // Bind to Warehouse Exchange - receive warehouse results
    @Bean
    public Binding stockReservedBinding(Queue warehouseResultQueue, TopicExchange warehouseExchange) {
        return BindingBuilder.bind(warehouseResultQueue)
                .to(warehouseExchange)
                .with(WAREHOUSE_STOCK_RESERVED_KEY);
    }
    
    @Bean
    public Binding stockInsufficientBinding(Queue warehouseResultQueue, TopicExchange warehouseExchange) {
        return BindingBuilder.bind(warehouseResultQueue)
                .to(warehouseExchange)
                .with(WAREHOUSE_STOCK_INSUFFICIENT_KEY);
    }
    
    @Bean
    public Binding stockDeductedBinding(Queue warehouseResultQueue, TopicExchange warehouseExchange) {
        return BindingBuilder.bind(warehouseResultQueue)
                .to(warehouseExchange)
                .with(WAREHOUSE_STOCK_DEDUCTED_KEY);
    }
    
    // Bind to Delivery Exchange - receive delivery status updates
    @Bean
    public Binding deliveryCreatedBinding(Queue deliveryStatusQueue, TopicExchange deliveryExchange) {
        return BindingBuilder.bind(deliveryStatusQueue)
                .to(deliveryExchange)
                .with(DELIVERY_CREATED_KEY);
    }
    
    @Bean
    public Binding deliveryShippedBinding(Queue deliveryStatusQueue, TopicExchange deliveryExchange) {
        return BindingBuilder.bind(deliveryStatusQueue)
                .to(deliveryExchange)
                .with(DELIVERY_SHIPPED_KEY);
    }
    
    @Bean
    public Binding deliveryInTransitBinding(Queue deliveryStatusQueue, TopicExchange deliveryExchange) {
        return BindingBuilder.bind(deliveryStatusQueue)
                .to(deliveryExchange)
                .with(DELIVERY_IN_TRANSIT_KEY);
    }
    
    @Bean
    public Binding deliveryDeliveredBinding(Queue deliveryStatusQueue, TopicExchange deliveryExchange) {
        return BindingBuilder.bind(deliveryStatusQueue)
                .to(deliveryExchange)
                .with(DELIVERY_DELIVERED_KEY);
    }
    
    @Bean
    public Binding deliveryLostBinding(Queue deliveryStatusQueue, TopicExchange deliveryExchange) {
        return BindingBuilder.bind(deliveryStatusQueue)
                .to(deliveryExchange)
                .with(DELIVERY_LOST_KEY);
    }
    
    @Bean
    public Binding deliveryDelayedBinding(Queue deliveryStatusQueue, TopicExchange deliveryExchange) {
        return BindingBuilder.bind(deliveryStatusQueue)
                .to(deliveryExchange)
                .with(DELIVERY_DELAYED_KEY);
    }

    // Wildcard binding to accept standardized keys like delivery.status.*
    @Bean
    public Binding deliveryStatusWildcardBinding(Queue deliveryStatusQueue, TopicExchange deliveryExchange) {
        return BindingBuilder.bind(deliveryStatusQueue)
                .to(deliveryExchange)
                .with("delivery.status.#");
    }
    
    // ========== Message Converter ==========
    
    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(mapper);
    }
    
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

    // Ensure @RabbitListener uses Jackson2JsonMessageConverter for JSON payloads
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        return factory;
    }
}


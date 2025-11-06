# Bank Service - Communication Architecture

This document describes the three communication methods implemented in the Bank Service to fulfill the project requirements.

## 📊 Three Communication Methods

### 1️⃣ REST API (Synchronous HTTP Communication)

**Type**: Request-Response, Synchronous  
**Protocol**: HTTP/HTTPS  
**Use Case**: Direct client-server communication requiring immediate response

#### Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/bank/payment` | POST | Process customer payment |
| `/api/bank/refund` | POST | Process order refund |
| `/api/bank/lost-package-refund` | POST | Process lost package refund |
| `/api/bank/health` | GET | Health check |

#### Example Usage

```bash
# Process Payment
curl -X POST http://localhost:8082/api/bank/payment \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORD-12345",
    "customerId": "CUST-001",
    "amount": 99.99,
    "currency": "AUD",
    "description": "Order payment"
  }'
```

#### Characteristics
- ✅ Immediate response
- ✅ Simple to implement and test
- ✅ Suitable for user-facing operations
- ⚠️ Blocking - client waits for response
- ⚠️ Tight coupling between services

---

### 2️⃣ RabbitMQ (Asynchronous Message Queue)

**Type**: Message-based, Asynchronous  
**Protocol**: AMQP  
**Provider**: CloudAMQP (Managed RabbitMQ Service)  
**Use Case**: Asynchronous processing, service decoupling, background tasks

#### Message Flow

```
┌─────────────────┐        RabbitMQ        ┌──────────────┐
│ Delivery Service│ ----[lost package]---> │ Bank Service │
│                 │                        │  (Listener)  │
└─────────────────┘                        └──────────────┘
                                                  ↓
                                           Process Refund
                                                  ↓
                                           ┌──────────────┐
                                           │ Email Service│
                                           │  (Listener)  │
                                           └──────────────┘
```

#### Queues and Exchanges

| Exchange | Queue | Routing Key | Purpose |
|----------|-------|-------------|---------|
| `bank.exchange` | `bank.refund.queue` | `bank.refund.*` | Receive refund requests |
| `delivery.exchange` | `bank.refund.queue` | `delivery.lost` | Receive lost package notifications |
| `order.exchange` | `bank.refund.queue` | `order.cancelled` | Receive order cancellations |

#### Message Listener

```java
@RabbitListener(queues = "bank.refund.queue")
public void handleRefundRequest(RefundMessage message) {
    // Process refund asynchronously
    RefundResponse response = bankService.processLostPackageRefund(request);
    
    // Publish success notification
    messagePublisher.publishRefundSuccess(response);
}
```

#### Message Publisher

```java
public void publishPaymentSuccess(PaymentResponse response) {
    rabbitTemplate.convertAndSend(
        "bank.exchange",
        "bank.payment.success",
        notification
    );
}
```

#### Characteristics
- ✅ Asynchronous - non-blocking
- ✅ Service decoupling
- ✅ Reliable delivery with retry mechanism
- ✅ Scalable - multiple consumers
- ✅ Message persistence
- ⚠️ More complex to set up
- ⚠️ Eventual consistency

#### CloudAMQP Setup

1. Sign up at https://www.cloudamqp.com/
2. Create free instance ("Little Lemur" plan)
3. Get connection details
4. Configure in `application.yml`:

```yaml
spring:
  rabbitmq:
    host: your-instance.cloudamqp.com
    port: 5672
    username: your-username
    password: your-password
    virtual-host: your-vhost
```

---

### 3️⃣ WebSocket (Real-time Bidirectional Communication)

**Type**: Real-time, Bidirectional  
**Protocol**: WebSocket (STOMP over WebSocket)  
**Use Case**: Real-time notifications, live updates to clients

#### WebSocket Endpoints

| Endpoint | Description |
|----------|-------------|
| `/ws` | WebSocket connection endpoint (with SockJS fallback) |
| `/user/{customerId}/queue/balance` | User-specific balance updates |
| `/user/{customerId}/queue/transactions` | User-specific transaction notifications |
| `/topic/transactions` | Broadcast transaction updates |

#### Message Flow

```
Bank Service (Backend)
    ↓ [WebSocket Push]
Frontend Client
    ↓ [Update UI in real-time]
User sees notification immediately
```

#### Real-time Notifications

**Balance Update Notification**:
```json
{
  "customerId": "CUST-001",
  "accountNumber": "ACC-12345",
  "oldBalance": 500.00,
  "newBalance": 400.00,
  "changeAmount": 100.00,
  "changeType": "DEBIT",
  "transactionId": "TXN-...",
  "reason": "Payment for order: ORD-12345",
  "currency": "AUD",
  "timestamp": "2024-01-15T10:30:00"
}
```

**Transaction Notification**:
```json
{
  "transactionId": "TXN-...",
  "customerId": "CUST-001",
  "transactionType": "PAYMENT",
  "amount": 100.00,
  "currency": "AUD",
  "status": "SUCCESS",
  "orderId": "ORD-12345",
  "message": "Payment processed successfully",
  "timestamp": "2024-01-15T10:30:00"
}
```

#### Backend Implementation

```java
@Service
public class WebSocketNotificationService {
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    public void sendBalanceUpdate(String customerId, ...) {
        // Send to specific user
        messagingTemplate.convertAndSendToUser(
            customerId,
            "/queue/balance",
            notification
        );
    }
}
```

#### Frontend Connection (JavaScript/React)

```javascript
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

// Connect to WebSocket
const socket = new SockJS('http://localhost:8082/api/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, () => {
    console.log('✅ WebSocket connected');
    
    // Subscribe to balance updates
    stompClient.subscribe('/user/queue/balance', (message) => {
        const update = JSON.parse(message.body);
        console.log('💰 Balance updated:', update);
        // Update UI with new balance
        updateBalanceDisplay(update.newBalance);
    });
    
    // Subscribe to transaction notifications
    stompClient.subscribe('/user/queue/transactions', (message) => {
        const notification = JSON.parse(message.body);
        console.log('📨 Transaction:', notification);
        // Show notification to user
        showNotification(notification.message);
    });
});
```

#### Characteristics
- ✅ Real-time updates
- ✅ Bidirectional communication
- ✅ Low latency
- ✅ Persistent connection
- ✅ Better user experience
- ⚠️ Requires persistent connection
- ⚠️ More resource intensive
- ⚠️ Requires client support

---

## 🎯 Comparison Table

| Feature | REST API | RabbitMQ | WebSocket |
|---------|----------|----------|-----------|
| **Communication** | Request-Response | Message Queue | Bidirectional Stream |
| **Timing** | Synchronous | Asynchronous | Real-time |
| **Connection** | Per-request | Persistent (broker) | Persistent (client) |
| **Direction** | Client → Server | Service ⇄ Service | Server ⇄ Client |
| **Use Case** | CRUD operations | Background processing | Live updates |
| **Response** | Immediate | Eventual | Immediate push |
| **Complexity** | Low | Medium | Medium |
| **Scalability** | Vertical | Horizontal | Moderate |

---

## 💡 Usage Scenarios in Bank Service

### Scenario 1: Customer Payment
```
1. Frontend → REST API → Bank Service (Synchronous)
2. Bank Service → RabbitMQ → Email Service (Async notification)
3. Bank Service → WebSocket → Frontend (Real-time balance update)
```

### Scenario 2: Lost Package Refund
```
1. Delivery Service → RabbitMQ → Bank Service (Async refund request)
2. Bank Service processes refund
3. Bank Service → RabbitMQ → Email Service (Async email)
4. Bank Service → WebSocket → Frontend (Real-time notification)
```

### Scenario 3: Balance Inquiry
```
1. Frontend → REST API → Bank Service (Synchronous query)
2. Bank Service returns current balance immediately
```

---

## 🧪 Testing the Communication Methods

### Test REST API
```bash
# Health check
curl http://localhost:8082/api/bank/health

# Process payment
curl -X POST http://localhost:8082/api/bank/payment \
  -H "Content-Type: application/json" \
  -d '{"orderId":"ORD-001","customerId":"CUST-001","amount":100,"currency":"AUD"}'
```

### Test RabbitMQ
```bash
# Send test refund message
curl -X POST "http://localhost:8082/api/test/send-refund-message?orderId=ORD-123&customerId=CUST-001&amount=50"

# Test RabbitMQ connection
curl http://localhost:8082/api/test/rabbitmq-connection
```

### Test WebSocket
Use the provided frontend example or tools like:
- WebSocket Test Client (browser extension)
- https://www.websocket.org/echo.html
- Postman (with WebSocket support)

---

## 📚 Configuration Files

### RabbitMQ Configuration
Location: `src/main/java/com/store/bank/config/RabbitMQConfig.java`

### WebSocket Configuration
Location: `src/main/java/com/store/bank/config/WebSocketConfig.java`

### Application Properties
Location: `src/main/resources/application.yml`

---

## ✅ Project Requirements Met

✅ **Communication Method 1**: REST API (Synchronous)  
✅ **Communication Method 2**: RabbitMQ (Asynchronous)  
✅ **Communication Method 3**: WebSocket (Real-time)  

**Total**: **3 communication methods** (meets full marks requirement)

---

## 🎓 Academic Justification

Each communication method serves a distinct purpose:

1. **REST API** - Necessary for client-server interactions requiring immediate feedback
2. **RabbitMQ** - Demonstrates understanding of microservices architecture and asynchronous processing
3. **WebSocket** - Shows ability to implement real-time features for enhanced user experience

This architecture demonstrates:
- ✅ Service decoupling
- ✅ Scalability considerations
- ✅ Fault tolerance (message queues)
- ✅ User experience optimization (real-time updates)
- ✅ Industry best practices


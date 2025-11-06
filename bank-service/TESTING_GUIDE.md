# Bank Service - Testing Guide

This guide provides instructions for testing all three communication methods in the Bank Service.

## 🧪 Prerequisites

Before testing, ensure:
- ✅ PostgreSQL is running with `bank_db` created
- ✅ Bank Service is running (`gradle bootRun`)
- ✅ CloudAMQP instance is configured
- ✅ Service is accessible at `http://localhost:8082`

## 1️⃣ Testing REST API (Synchronous Communication)

### Health Check
```bash
curl http://localhost:8082/api/bank/health
```
**Expected Response**: `"Bank Service is running"`

### Process Payment
```bash
curl -X POST http://localhost:8082/api/bank/payment \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORD-12345",
    "customerId": "CUST-001",
    "amount": 99.99,
    "currency": "AUD",
    "description": "Test payment for order"
  }'
```

**Expected Response**:
```json
{
  "transactionId": "TXN-...",
  "orderId": "ORD-12345",
  "customerId": "CUST-001",
  "amount": 99.99,
  "currency": "AUD",
  "status": "SUCCESS",
  "reference": "TXN-...",
  "processedAt": "2024-01-15T10:30:00",
  "message": "Payment processed successfully"
}
```

### Process Refund
```bash
curl -X POST http://localhost:8082/api/bank/refund \
  -H "Content-Type: application/json" \
  -d '{
    "transactionId": "TXN-xxx",
    "orderId": "ORD-12345",
    "refundAmount": 50.00,
    "reason": "Customer request"
  }'
```

### Process Lost Package Refund
```bash
curl -X POST http://localhost:8082/api/bank/lost-package-refund \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORD-12345",
    "customerId": "CUST-001",
    "refundAmount": 99.99,
    "currency": "AUD",
    "deliveryId": "DEL-123",
    "lostReason": "Package lost during delivery"
  }'
```

---

## 2️⃣ Testing RabbitMQ (Asynchronous Communication)

### Test RabbitMQ Connection
```bash
curl http://localhost:8082/api/test/rabbitmq-connection
```
**Expected Response**: `"RabbitMQ connection is working"`

### Send Test Refund Message to Queue
```bash
curl -X POST "http://localhost:8082/api/test/send-refund-message?orderId=ORD-TEST-001&customerId=CUST-001&amount=150.00"
```

**Expected Response**: `"Test refund message sent to queue. Check logs for processing."`

### Check Application Logs
After sending the message, check the application logs for:

```
📨 Received refund message from queue: RefundMessage(orderId=ORD-TEST-001, ...)
Processing refund type: LOST_PACKAGE
✅ Refund processed successfully: transactionId=TXN-..., orderId=ORD-TEST-001
```

### Test Message Flow
1. **Send message** → Queue
2. **Listener receives** → Processes automatically
3. **Refund processed** → Database updated
4. **Success notification** → Published to exchange

---

## 3️⃣ Testing WebSocket (Real-time Communication)

### Option 1: Using Browser Console (Simple Test)

1. Open browser to `http://localhost:8082`
2. Open Developer Tools (F12)
3. Go to Console tab
4. Paste the following code:

```javascript
// Load SockJS and STOMP (if not already loaded)
const script1 = document.createElement('script');
script1.src = 'https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js';
document.head.appendChild(script1);

script1.onload = () => {
  const script2 = document.createElement('script');
  script2.src = 'https://cdn.jsdelivr.net/npm/@stomp/stompjs@7/dist/stomp.umd.min.js';
  document.head.appendChild(script2);
  
  script2.onload = () => {
    // Connect to WebSocket
    const socket = new SockJS('http://localhost:8082/api/ws');
    const stompClient = StompJS.Stomp.over(socket);
    
    stompClient.connect({}, (frame) => {
      console.log('✅ Connected to WebSocket:', frame);
      
      // Subscribe to balance updates (replace CUST-001 with actual customer ID)
      stompClient.subscribe('/user/CUST-001/queue/balance', (message) => {
        console.log('💰 Balance Update:', JSON.parse(message.body));
      });
      
      // Subscribe to transaction notifications
      stompClient.subscribe('/user/CUST-001/queue/transactions', (message) => {
        console.log('📨 Transaction Notification:', JSON.parse(message.body));
      });
      
      // Subscribe to all transactions (broadcast)
      stompClient.subscribe('/topic/transactions', (message) => {
        console.log('📡 Broadcast Transaction:', JSON.parse(message.body));
      });
      
      console.log('✅ Subscribed to all channels. Now process a payment to see notifications!');
    });
  };
};
```

5. Process a payment using REST API (see above)
6. Watch the console for real-time notifications

**Expected Console Output**:
```
✅ Connected to WebSocket
✅ Subscribed to all channels
💰 Balance Update: {customerId: "CUST-001", oldBalance: 1000, newBalance: 900.01, ...}
📨 Transaction Notification: {transactionId: "TXN-...", status: "SUCCESS", ...}
📡 Broadcast Transaction: {transactionId: "TXN-...", ...}
```

### Option 2: Using HTML Test Client

Create a file `websocket-test.html`:

```html
<!DOCTYPE html>
<html>
<head>
    <title>WebSocket Test Client</title>
    <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/@stomp/stompjs@7/dist/stomp.umd.min.js"></script>
</head>
<body>
    <h1>Bank Service WebSocket Test</h1>
    <div id="status">Disconnected</div>
    <input type="text" id="customerId" placeholder="Customer ID" value="CUST-001">
    <button onclick="connect()">Connect</button>
    <button onclick="disconnect()">Disconnect</button>
    <h2>Messages:</h2>
    <div id="messages" style="border: 1px solid black; padding: 10px; height: 400px; overflow-y: scroll;"></div>

    <script>
        let stompClient = null;

        function connect() {
            const customerId = document.getElementById('customerId').value;
            const socket = new SockJS('http://localhost:8082/api/ws');
            stompClient = StompJS.Stomp.over(socket);

            stompClient.connect({}, (frame) => {
                document.getElementById('status').textContent = 'Connected';
                document.getElementById('status').style.color = 'green';
                addMessage('✅ Connected to WebSocket');

                // Subscribe to balance updates
                stompClient.subscribe('/user/' + customerId + '/queue/balance', (message) => {
                    const data = JSON.parse(message.body);
                    addMessage('💰 Balance Update: ' + JSON.stringify(data, null, 2));
                });

                // Subscribe to transaction notifications
                stompClient.subscribe('/user/' + customerId + '/queue/transactions', (message) => {
                    const data = JSON.parse(message.body);
                    addMessage('📨 Transaction: ' + JSON.stringify(data, null, 2));
                });

                // Subscribe to broadcast
                stompClient.subscribe('/topic/transactions', (message) => {
                    const data = JSON.parse(message.body);
                    addMessage('📡 Broadcast: ' + JSON.stringify(data, null, 2));
                });
            });
        }

        function disconnect() {
            if (stompClient !== null) {
                stompClient.disconnect();
            }
            document.getElementById('status').textContent = 'Disconnected';
            document.getElementById('status').style.color = 'red';
            addMessage('❌ Disconnected');
        }

        function addMessage(message) {
            const messagesDiv = document.getElementById('messages');
            const time = new Date().toLocaleTimeString();
            messagesDiv.innerHTML += `<p><strong>${time}:</strong> ${message}</p>`;
            messagesDiv.scrollTop = messagesDiv.scrollHeight;
        }
    </script>
</body>
</html>
```

1. Open `websocket-test.html` in browser
2. Enter customer ID (e.g., `CUST-001`)
3. Click "Connect"
4. Process a payment via REST API
5. Watch for real-time notifications in the messages area

### Option 3: Using Postman (Advanced)

1. Open Postman
2. Create a new WebSocket request
3. URL: `ws://localhost:8082/api/ws`
4. Connect
5. Send STOMP CONNECT frame:
```
CONNECT
accept-version:1.0,1.1,2.0
heart-beat:10000,10000

^@
```

6. Subscribe to a destination:
```
SUBSCRIBE
id:sub-0
destination:/user/CUST-001/queue/balance

^@
```

7. Process a payment and watch for incoming messages

---

## 🎯 Complete Integration Test

Test all three communication methods together:

### Step 1: Connect to WebSocket
```javascript
// Use browser console or HTML test client (see above)
// Connect to WebSocket and subscribe to channels
```

### Step 2: Send RabbitMQ Message
```bash
curl -X POST "http://localhost:8082/api/test/send-refund-message?orderId=ORD-FULL-TEST&customerId=CUST-001&amount=200.00"
```

### Step 3: Process REST Payment
```bash
curl -X POST http://localhost:8082/api/bank/payment \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORD-FULL-TEST-2",
    "customerId": "CUST-001",
    "amount": 50.00,
    "currency": "AUD",
    "description": "Full integration test"
  }'
```

### Expected Results:

1. **REST API**:
   - Immediate JSON response with transaction details
   - HTTP 200 OK status

2. **RabbitMQ**:
   - Message appears in application logs
   - Refund processed asynchronously
   - Success notification published

3. **WebSocket**:
   - Real-time balance update notification
   - Real-time transaction notification
   - Broadcast to all subscribers

---

## 📊 Verification Checklist

After running all tests, verify:

- [ ] REST API responds immediately with JSON
- [ ] RabbitMQ messages are processed (check logs)
- [ ] WebSocket notifications appear in real-time
- [ ] Database has new transaction records
- [ ] CloudAMQP dashboard shows message activity
- [ ] No errors in application logs

---

## 🐛 Troubleshooting

### REST API Issues
- **404 Not Found**: Check URL path includes `/api` prefix
- **500 Error**: Check database connection and credentials
- **Connection Refused**: Ensure service is running on port 8082

### RabbitMQ Issues
- **Connection Failed**: Verify CloudAMQP credentials in `application.yml`
- **Messages Not Processing**: Check application logs for errors
- **No Messages in Queue**: Verify queue and exchange bindings

### WebSocket Issues
- **Connection Failed**: Check CORS settings, ensure `/ws` endpoint is accessible
- **No Notifications**: Verify correct customer ID in subscription
- **Connection Drops**: Check network stability, increase heartbeat interval

---

## 📝 Notes

- All endpoints require the `/api` context path prefix
- Customer accounts must exist in database before processing payments
- Initial balance is 0.00 for new accounts
- WebSocket connections are stateful - reconnect if connection drops
- RabbitMQ messages are processed asynchronously with retry mechanism

---

## 🎓 For Academic Evaluation

This testing guide demonstrates:
1. ✅ **REST API** - Synchronous communication
2. ✅ **RabbitMQ** - Asynchronous messaging  
3. ✅ **WebSocket** - Real-time bidirectional communication

All three methods are fully functional and can be tested independently or as an integrated system.


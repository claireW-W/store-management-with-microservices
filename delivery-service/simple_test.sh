#!/bin/bash

# Simple Delivery Service API test script

echo "🚀 Testing Delivery Service API..."
echo "================================"

BASE_URL=" http://localhost:8082/api"
ORDER_ID="ORDER-TEST-$(date +%s)"
CUSTOMER_ID="CUSTOMER-TEST-$(date +%s)"

# Test 1: Create delivery request
echo -e "\n📦 Test 1: Create delivery request"
echo "URL: POST $BASE_URL/delivery/request"
response1=$(curl -s -X POST "$BASE_URL/delivery/request" \
    -H "Content-Type: application/json" \
    -d '{
        "orderId": "'$ORDER_ID'",
        "customerId": "'$CUSTOMER_ID'", 
        "shippingAddress": "123 Test Street, Sydney NSW 2000, Australia",
        "warehouseId": 1,
        "carrier": "Australia Post",
        "notes": "Test delivery"
    }')

echo "Response: $response1"

# Extract deliveryId
DELIVERY_ID=$(echo "$response1" | grep -o '"deliveryId":"[^"]*"' | cut -d'"' -f4)
echo "Delivery ID: $DELIVERY_ID"

# Test 2: Update status
if [ -n "$DELIVERY_ID" ]; then
    echo -e "\n📦 Test 2: Update delivery status"
    echo "URL: PUT $BASE_URL/delivery/$DELIVERY_ID/status"
    response2=$(curl -s -X PUT "$BASE_URL/delivery/$DELIVERY_ID/status" \
        -H "Content-Type: application/json" \
        -d '{
            "status": "PICKED_UP",
            "location": "Sydney Warehouse",
            "notes": "Package picked up"
        }')
    echo "Response: $response2"
else
    echo "❌ Unable to get Delivery ID, skipping status update test"
fi

# Test 3: Handle package lost
if [ -n "$DELIVERY_ID" ]; then
    echo -e "\n📦 Test 3: Handle package lost"
    echo "URL: POST $BASE_URL/delivery/$DELIVERY_ID/lost"
    response3=$(curl -s -X POST "$BASE_URL/delivery/$DELIVERY_ID/lost" \
        -H "Content-Type: application/json" \
        -d '{
            "reason": "Package lost during transit",
            "notes": "Last seen at Sydney facility"
        }')
    echo "Response: $response3"
else
    echo "❌ Unable to get Delivery ID, skipping package lost test"
fi

# Test 4: Query lost packages
echo -e "\n📦 Test 4: Query lost packages"
echo "URL: GET $BASE_URL/delivery/lost-packages"
response4=$(curl -s -X GET "$BASE_URL/delivery/lost-packages")
echo "Response: $response4"

# Test 5: Set lost probability
echo -e "\n📦 Test 5: Set lost probability"
echo "URL: PUT $BASE_URL/delivery/config/lost-probability"
response5=$(curl -s -X PUT "$BASE_URL/delivery/config/lost-probability" \
    -H "Content-Type: application/json" \
    -d '{
        "probability": 0.1
    }')
echo "Response: $response5"

# Test 6: Get lost probability
echo -e "\n📦 Test 6: Get lost probability"
echo "URL: GET $BASE_URL/delivery/config/lost-probability"
response6=$(curl -s -X GET "$BASE_URL/delivery/config/lost-probability")
echo "Response: $response6"

echo -e "\n✅ Testing completed!"
echo "================================"

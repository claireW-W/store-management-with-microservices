// Order details page component
import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { OrderResponse } from '../types';
import { orderService } from '../services';
import { formatCurrency, formatDate, getOrderStatusConfig, canCancelOrder, isActiveOrder } from '../utils/helpers';
import { usePolling } from '../hooks';
import './OrderDetails.css';

const OrderDetails: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [order, setOrder] = useState<OrderResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [cancelling, setCancelling] = useState(false);

  const fetchOrder = useCallback(async () => {
    if (!id) return;
    
    try {
      const fetchedOrder = await orderService.getOrderById(parseInt(id));
      setOrder(fetchedOrder);
      setError(null);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load order');
    }
  }, [id]);

  // Use polling for active orders
  const { data: polledOrder, loading: pollingLoading } = usePolling(
    fetchOrder,
    5000, // 5 seconds
    order ? isActiveOrder(order.status) : false, // Only poll if order is active
    (error) => {
      console.error('Polling error:', error);
    }
  );

  // Update order when polling data changes
  useEffect(() => {
    if (polledOrder) {
      setOrder(polledOrder);
    }
  }, [polledOrder]);

  // Initial fetch
  useEffect(() => {
    const initialFetch = async () => {
      try {
        setLoading(true);
        await fetchOrder();
      } finally {
        setLoading(false);
      }
    };

    initialFetch();
  }, [id, fetchOrder]);

  const handleCancelOrder = async () => {
    if (!order || !canCancelOrder(order.status)) return;

    const confirmed = window.confirm(
      `Are you sure you want to cancel order #${order.orderNumber}? This action cannot be undone.`
    );

    if (!confirmed) return;

    try {
      setCancelling(true);
      await orderService.cancelOrder(order.id, {
        reason: 'Cancelled by customer'
      });
      
      // Refresh order data
      await fetchOrder();
      alert('Order cancelled successfully');
    } catch (err) {
      alert(`Failed to cancel order: ${err instanceof Error ? err.message : 'Unknown error'}`);
    } finally {
      setCancelling(false);
    }
  };

  if (loading) {
    return (
      <div className="order-details-container">
        <div className="loading-container">
          <div className="loading-spinner"></div>
          <p>Loading order details...</p>
        </div>
      </div>
    );
  }

  if (error || !order) {
    return (
      <div className="order-details-container">
        <div className="error-container">
          <h2>Order Not Found</h2>
          <p>{error || 'The order you are looking for does not exist.'}</p>
          <button 
            className="back-btn"
            onClick={() => navigate('/orders')}
          >
            Back to Orders
          </button>
        </div>
      </div>
    );
  }

  const statusConfig = getOrderStatusConfig(order.status);

  return (
    <div className="order-details-container">
      <div className="order-details-header">
        <button 
          className="back-btn"
          onClick={() => navigate('/orders')}
        >
          ← Back to Orders
        </button>
        
        <div className="order-title">
          <h1>Order #{order.orderNumber}</h1>
          <div className="order-status-container">
            <div 
              className="order-status-badge"
              style={{
                color: statusConfig.color,
                backgroundColor: statusConfig.backgroundColor
              }}
            >
              {statusConfig.label}
            </div>
            {pollingLoading && (
              <div className="polling-indicator">
                <div className="polling-dot"></div>
                <span>Updating...</span>
              </div>
            )}
          </div>
        </div>
      </div>

      <div className="order-details-content">
        {/* Order Items */}
        <div className="order-section">
          <h2>Order Items</h2>
          <div className="order-items">
            {order.items.map((item, index) => (
              <div key={index} className="order-item">
                <div className="item-info">
                  <h3>{item.productName || item.product?.name || `Product ${item.productId}`}</h3>
                  <p className="item-description">
                    {item.productDescription || item.product?.description || 'Product description not available'}
                  </p>
                  <div className="item-details">
                    <span className="item-sku">SKU: {item.productSku || item.product?.sku || 'N/A'}</span>
                    <span className="item-weight">
                      Weight: {item.productWeight ? `${item.productWeight}kg` : (item.product?.weight ? `${item.product.weight}kg` : 'N/A')}
                    </span>
                  </div>
                </div>
                <div className="item-pricing">
                  <div className="item-quantity">Quantity: {item.quantity}</div>
                  <div className="item-unit-price">
                    Unit Price: {formatCurrency(item.unitPrice)}
                  </div>
                  <div className="item-total-price">
                    Total: {formatCurrency(item.totalPrice)}
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Shipping Address */}
        <div className="order-section">
          <h2>Shipping Address</h2>
          <div className="address-info">
            <div className="address-line">{order.shippingAddress.street}</div>
            <div className="address-line">
              {order.shippingAddress.suburb}, {order.shippingAddress.state} {order.shippingAddress.postcode}
            </div>
            <div className="address-line">{order.shippingAddress.country}</div>
          </div>
        </div>

        {/* Payment Information */}
        <div className="order-section">
          <h2>💳 Payment Information</h2>
          <div className="payment-info">
            <div className="payment-method">
              <strong>Payment Method:</strong> {order.paymentMethod}
            </div>
            <div className="payment-status">
              <strong>Payment Status:</strong> <span className={`status-badge status-${order.paymentStatus.toLowerCase()}`}>{order.paymentStatus}</span>
            </div>
            {order.paymentTransactionId && (
              <div className="payment-transaction">
                <strong>Transaction ID:</strong> <code>{order.paymentTransactionId}</code>
              </div>
            )}
          </div>
        </div>

        {/* Delivery Tracking Information */}
        {order.deliveryId && (
          <div className="order-section delivery-tracking-section">
            <h2>📦 Delivery Tracking</h2>
            <div className="delivery-tracking">
              <div className="delivery-header">
                <div className="delivery-id">
                  <strong>Delivery ID:</strong> <code>{order.deliveryId}</code>
                </div>
                {order.deliveryInfo?.trackingNumber && (
                  <div className="tracking-number">
                    <strong>Tracking Number:</strong> <code>{order.deliveryInfo.trackingNumber}</code>
                  </div>
                )}
              </div>
              
              {order.deliveryInfo && (
                <>
                  <div className="delivery-status-indicator">
                    <div className={`delivery-status-badge status-${order.deliveryInfo.status.toLowerCase()}`}>
                      {order.deliveryInfo.status.replace(/_/g, ' ')}
                    </div>
                  </div>
                  
                  <div className="delivery-progress">
                    <div className={`progress-step ${['PENDING_PICKUP', 'PICKED_UP', 'IN_TRANSIT', 'OUT_FOR_DELIVERY', 'DELIVERED'].includes(order.deliveryInfo.status) ? 'completed' : ''}`}>
                      <div className="step-marker">📦</div>
                      <div className="step-label">Pending Pickup</div>
                    </div>
                    
                    <div className="progress-line"></div>
                    
                    <div className={`progress-step ${['PICKED_UP', 'IN_TRANSIT', 'OUT_FOR_DELIVERY', 'DELIVERED'].includes(order.deliveryInfo.status) ? 'completed' : ''}`}>
                      <div className="step-marker">🚚</div>
                      <div className="step-label">Picked Up</div>
                    </div>
                    
                    <div className="progress-line"></div>
                    
                    <div className={`progress-step ${['IN_TRANSIT', 'OUT_FOR_DELIVERY', 'DELIVERED'].includes(order.deliveryInfo.status) ? 'completed' : ''}`}>
                      <div className="step-marker">🛣️</div>
                      <div className="step-label">In Transit</div>
                    </div>
                    
                    <div className="progress-line"></div>
                    
                    <div className={`progress-step ${['DELIVERED'].includes(order.deliveryInfo.status) ? 'completed' : ''}`}>
                      <div className="step-marker">✅</div>
                      <div className="step-label">Delivered</div>
                    </div>
                  </div>
                  
                  <div className="delivery-details-grid">
                    {order.deliveryInfo.carrier && (
                      <div className="delivery-detail-item">
                        <strong>Carrier:</strong> {order.deliveryInfo.carrier}
                      </div>
                    )}
                    {order.deliveryInfo.estimatedDelivery && (
                      <div className="delivery-detail-item">
                        <strong>Estimated Delivery:</strong> {formatDate(order.deliveryInfo.estimatedDelivery)}
                      </div>
                    )}
                    {order.deliveryInfo.actualDelivery && (
                      <div className="delivery-detail-item">
                        <strong>Actual Delivery:</strong> {formatDate(order.deliveryInfo.actualDelivery)}
                      </div>
                    )}
                    {order.deliveryInfo.currentLocation && (
                      <div className="delivery-detail-item">
                        <strong>Current Location:</strong> {order.deliveryInfo.currentLocation}
                      </div>
                    )}
                  </div>
                </>
              )}
            </div>
          </div>
        )}

        {/* Order Summary */}
        <div className="order-section">
          <h2>Order Summary</h2>
          <div className="order-summary">
            <div className="summary-row">
              <span>Order Date:</span>
              <span>{formatDate(order.createdAt)}</span>
            </div>
            <div className="summary-row">
              <span>Last Updated:</span>
              <span>{formatDate(order.updatedAt)}</span>
            </div>
            {order.deliveryId && (
              <div className="summary-row">
                <span>Delivery ID:</span>
                <span className="delivery-id">{order.deliveryId}</span>
              </div>
            )}
            <div className="summary-row total">
              <span>Total Amount:</span>
              <span>{formatCurrency(order.totalAmount)}</span>
            </div>
            {order.notes && (
              <div className="summary-row notes">
                <span>Notes:</span>
                <span>{order.notes}</span>
              </div>
            )}
          </div>
        </div>

        {/* Status History */}
        {order.statusHistory && order.statusHistory.length > 0 && (
          <div className="order-section">
            <h2>Status History</h2>
            <div className="status-timeline">
              {order.statusHistory.map((status, index) => (
                <div key={index} className="timeline-item">
                  <div className="timeline-marker"></div>
                  <div className="timeline-content">
                    <div className="timeline-status">
                      {getOrderStatusConfig(status.status).label}
                    </div>
                    <div className="timeline-date">
                      {formatDate(status.timestamp)}
                    </div>
                    {status.notes && (
                      <div className="timeline-notes">
                        {status.notes}
                      </div>
                    )}
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Action Buttons */}
        <div className="order-actions">
          {canCancelOrder(order.status) && (
            <button 
              className="cancel-btn"
              onClick={handleCancelOrder}
              disabled={cancelling}
            >
              {cancelling ? 'Cancelling...' : 'Cancel Order'}
            </button>
          )}
        </div>
      </div>
    </div>
  );
};

export default OrderDetails;

// Order card component
import React from 'react';
import { Link } from 'react-router-dom';
import { OrderResponse } from '../../types';
import { formatCurrency, formatDate, getOrderStatusConfig } from '../../utils/helpers';
import './OrderCard.css';

interface OrderCardProps {
  order: OrderResponse;
}

const OrderCard: React.FC<OrderCardProps> = ({ order }) => {
  const statusConfig = getOrderStatusConfig(order.status);

  return (
    <Link to={`/orders/${order.id}`} className="order-card-link">
      <div className="order-card">
        <div className="order-header">
          <div className="order-number">
            Order #{order.orderNumber}
          </div>
          <div 
            className="order-status"
            style={{
              color: statusConfig.color,
              backgroundColor: statusConfig.backgroundColor
            }}
          >
            {statusConfig.label}
          </div>
        </div>

        <div className="order-content">
          <div className="order-items">
            {order.items.map((item, index) => (
              <div key={index} className="order-item">
                <span className="item-name">
                  {item.productName || item.product?.name || `Product ${item.productId}`}
                </span>
                <span className="item-quantity">
                  x{item.quantity}
                </span>
                <span className="item-price">
                  {formatCurrency(item.totalPrice)}
                </span>
              </div>
            ))}
          </div>

          <div className="order-summary">
            <div className="order-total">
              Total: {formatCurrency(order.totalAmount)}
            </div>
            <div className="order-date">
              {formatDate(order.createdAt)}
            </div>
          </div>
        </div>

        <div className="order-footer">
          <div className="order-payment">
            Payment: {order.paymentMethod}
          </div>
          <div className="order-delivery">
            {order.deliveryId && (
              <span className="delivery-id">
                Delivery ID: {order.deliveryId}
              </span>
            )}
          </div>
        </div>
      </div>
    </Link>
  );
};

export default OrderCard;

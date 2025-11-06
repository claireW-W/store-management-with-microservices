// Order service
import api from './api';
import { 
  CreateOrderRequest, 
  OrderResponse, 
  CancelOrderRequest, 
  CancelOrderResponse 
} from '../types';

export const orderService = {
  // Create order
  async createOrder(orderData: CreateOrderRequest): Promise<OrderResponse> {
    const response = await api.post<OrderResponse>('/orders', orderData);
    return response.data;
  },

  // Get all user orders
  async getOrders(): Promise<OrderResponse[]> {
    const response = await api.get<OrderResponse[]>('/orders');
    return response.data;
  },

  // Get single order details
  async getOrderById(orderId: number): Promise<OrderResponse> {
    const response = await api.get<OrderResponse>(`/orders/${orderId}`);
    return response.data;
  },

  // Cancel order
  async cancelOrder(orderId: number, cancelData?: CancelOrderRequest): Promise<CancelOrderResponse> {
    const response = await api.post<CancelOrderResponse>(
      `/orders/${orderId}/cancel`, 
      cancelData || {}
    );
    return response.data;
  },
};

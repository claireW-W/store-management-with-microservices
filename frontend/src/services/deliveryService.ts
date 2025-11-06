// Delivery service
import api from './api';
import { DeliveryInfo } from '../types';

export const deliveryService = {
  // Get delivery information by delivery ID
  async getDeliveryInfo(deliveryId: string): Promise<DeliveryInfo> {
    const response = await api.get<DeliveryInfo>(`/delivery/${deliveryId}`);
    return response.data;
  },

  // Get delivery information by order ID
  async getDeliveryByOrderId(orderId: string): Promise<DeliveryInfo> {
    const response = await api.get<DeliveryInfo>(`/delivery/order/${orderId}`);
    return response.data;
  },
};




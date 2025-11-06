// TypeScript type definitions
// Type definitions matching backend API responses

export interface User {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  phone?: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  tokenType: string;
  expiresIn: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  loginTime: string;
  message: string;
}

export interface SignupRequest {
  username: string;
  password: string;
  email: string;
  firstName?: string;
  lastName?: string;
  phone?: string;
}

export interface SignupResponse {
  success: boolean;
  message: string;
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
  userId: number;
  createdAt: string;
}

export interface Product {
  id: number;
  name: string;
  description: string;
  price: number;
  categoryId: number;
  category?: {
    id: number;
    name: string;
    description: string;
  };
  sku: string;
  weight?: number;
  dimensions?: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface OrderItem {
  id: number;
  productId: number;
  product?: Product;
  productName?: string;  // Added: product name from backend
  productSku?: string;   // Added: product SKU from backend
  productDescription?: string;  // Added: product description from backend
  productWeight?: number;       // Added: product weight from backend
  quantity: number;
  unitPrice: number;
  totalPrice: number;
}

export interface Address {
  street: string;
  suburb: string;
  state: string;
  postcode: string;
  country: string;
}

export interface CreateOrderRequest {
  items: Array<{
    productId: number;
    quantity: number;
  }>;
  shippingAddress: Address;
  paymentMethod: string;
  notes?: string;
}

// NEW: Delivery tracking information
export interface DeliveryInfo {
  deliveryId: string;
  trackingNumber: string;
  status: DeliveryStatus;
  carrier: string;
  estimatedDelivery?: string;
  actualDelivery?: string;
  currentLocation?: string;
  statusHistory?: DeliveryStatusHistory[];
}

export type DeliveryStatus = 
  | 'PENDING_PICKUP'
  | 'PICKED_UP'
  | 'IN_TRANSIT'
  | 'OUT_FOR_DELIVERY'
  | 'DELIVERED'
  | 'FAILED'
  | 'LOST';

export interface DeliveryStatusHistory {
  status: DeliveryStatus;
  timestamp: string;
  location?: string;
  notes?: string;
}

export interface OrderResponse {
  id: number;
  orderNumber: string;
  userId: number;
  status: OrderStatus;
  totalAmount: number;
  paymentMethod: string;
  paymentStatus: string;
  paymentTransactionId?: string;
  shippingAddress: Address;
  billingAddress?: Address;
  notes?: string;
  deliveryId?: string;
  deliveryInfo?: DeliveryInfo;  // NEW: Full delivery information
  items: OrderItem[];
  statusHistory: OrderStatusHistory[];
  createdAt: string;
  updatedAt: string;
}

export interface CancelOrderRequest {
  reason?: string;
}

export interface CancelOrderResponse {
  success: boolean;
  message: string;
  orderId: number;
  newStatus: OrderStatus;
}

export interface OrderStatusHistory {
  id: number;
  orderId: number;
  status: OrderStatus;
  timestamp: string;
  notes?: string;
}

export type OrderStatus = 
  | 'PENDING'           // Pending
  | 'PAID'              // Paid
  | 'PROCESSING'        // Processing
  | 'PENDING_PICKUP'    // Delivery request received
  | 'PICKED_UP'         // Package picked up by carrier
  | 'SHIPPED'           // Shipped (NEW)
  | 'IN_TRANSIT'        // In Transit
  | 'DELIVERED'         // Delivered
  | 'LOST'              // Lost (NEW)
  | 'CANCELLED'         // Cancelled
  | 'REFUNDED';         // Refunded

export interface ApiError {
  error: string;
  message: string;
  requestId?: string;
  timestamp?: string;
}

// NEW: WebSocket notification types
export interface PaymentNotification {
  orderNumber: string;
  transactionId: string;
  status: 'SUCCESS' | 'FAILED';
  amount: number;
  message: string;
  timestamp: string;
}

export interface DeliveryNotification {
  orderNumber: string;
  deliveryId: string;
  trackingNumber: string;
  status: DeliveryStatus;
  message: string;
  timestamp: string;
}

export interface Notification {
  id: string;
  type: 'success' | 'error' | 'info' | 'warning';
  message: string;
  timestamp: string;
}

// Utility types
export interface StatusConfig {
  label: string;
  color: string;
  backgroundColor: string;
}

export interface PollingConfig {
  interval: number; // milliseconds
  enabled: boolean;
  onError?: (error: Error) => void;
}

// Balance types
export interface Balance {
  customerId: string;
  accountNumber: string;
  accountHolderName: string;
  balance: number;
  currency: string;
  isActive: boolean;
}

// Inventory types
export interface Inventory {
  warehouseId: number;
  warehouseCode?: string;
  warehouseName?: string;
  productId: number;
  availableQuantity: number;
  reservedQuantity: number;
  totalQuantity: number;
}

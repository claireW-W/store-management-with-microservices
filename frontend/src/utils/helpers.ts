// Utility functions collection
import { OrderStatus, StatusConfig, ApiError } from '../types';

// Currency formatting
export const formatCurrency = (amount: number): string => {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
  }).format(amount);
};

// Date formatting
export const formatDate = (dateString: string): string => {
  const date = new Date(dateString);
  return new Intl.DateTimeFormat('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date);
};

// Relative time formatting
export const formatRelativeTime = (dateString: string): string => {
  const date = new Date(dateString);
  const now = new Date();
  const diffInSeconds = Math.floor((now.getTime() - date.getTime()) / 1000);

  if (diffInSeconds < 60) {
    return 'Just now';
  } else if (diffInSeconds < 3600) {
    const minutes = Math.floor(diffInSeconds / 60);
    return `${minutes} minute${minutes > 1 ? 's' : ''} ago`;
  } else if (diffInSeconds < 86400) {
    const hours = Math.floor(diffInSeconds / 3600);
    return `${hours} hour${hours > 1 ? 's' : ''} ago`;
  } else {
    const days = Math.floor(diffInSeconds / 86400);
    return `${days} day${days > 1 ? 's' : ''} ago`;
  }
};

// Error message extraction
export const getErrorMessage = (error: unknown): string => {
  if (error && typeof error === 'object' && 'message' in error) {
    return (error as ApiError).message;
  }
  if (typeof error === 'string') {
    return error;
  }
  return 'An unexpected error occurred';
};

// Order status configuration
export const getOrderStatusConfig = (status: OrderStatus): StatusConfig => {
  const statusConfigs: Record<OrderStatus, StatusConfig> = {
    PENDING: {
      label: 'Pending',
      color: '#f59e0b',
      backgroundColor: '#fef3c7',
    },
    PAID: {
      label: 'Paid',
      color: '#10b981',
      backgroundColor: '#d1fae5',
    },
    PROCESSING: {
      label: 'Processing',
      color: '#3b82f6',
      backgroundColor: '#dbeafe',
    },
    PENDING_PICKUP: {
      label: 'Pending Pickup',
      color: '#6b7280',
      backgroundColor: '#f3f4f6',
    },
    PICKED_UP: {
      label: 'Picked Up',
      color: '#2563eb',
      backgroundColor: '#dbeafe',
    },
    SHIPPED: {
      label: 'Shipped',
      color: '#8b5cf6',
      backgroundColor: '#ede9fe',
    },
    IN_TRANSIT: {
      label: 'In Transit',
      color: '#8b5cf6',
      backgroundColor: '#ede9fe',
    },
    DELIVERED: {
      label: 'Delivered',
      color: '#10b981',
      backgroundColor: '#d1fae5',
    },
    LOST: {
      label: 'Lost',
      color: '#ef4444',
      backgroundColor: '#fee2e2',
    },
    CANCELLED: {
      label: 'Cancelled',
      color: '#ef4444',
      backgroundColor: '#fee2e2',
    },
    REFUNDED: {
      label: 'Refunded',
      color: '#6b7280',
      backgroundColor: '#f3f4f6',
    },
  };

  return statusConfigs[status] || {
    label: status,
    color: '#6b7280',
    backgroundColor: '#f3f4f6',
  };
};

// Check if order status is active
export const isActiveOrder = (status: OrderStatus): boolean => {
  return ['PENDING', 'PAID', 'PROCESSING', 'PENDING_PICKUP', 'PICKED_UP', 'SHIPPED', 'IN_TRANSIT'].includes(status);
};

// Check if order can be cancelled
export const canCancelOrder = (status: OrderStatus): boolean => {
  return ['PENDING', 'PAID', 'PROCESSING'].includes(status);
};

// Format order number display
export const formatOrderNumber = (orderNumber: string): string => {
  // If order number is already formatted, return directly
  if (orderNumber.includes('-')) {
    return orderNumber;
  }
  
  // Otherwise format as XXX-XXXX-XXXX
  const cleaned = orderNumber.replace(/\D/g, '');
  if (cleaned.length >= 12) {
    return `${cleaned.slice(0, 3)}-${cleaned.slice(3, 7)}-${cleaned.slice(7, 11)}`;
  }
  
  return orderNumber;
};

// Validate email format
export const isValidEmail = (email: string): boolean => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
};

// Validate postal code format
export const isValidPostalCode = (postalCode: string): boolean => {
  // Support 4-digit postal code format (for Australia and simplified US format)
  const postalRegex = /^\d{4}$/;
  return postalRegex.test(postalCode);
};

// Validate phone number format
export const isValidPhoneNumber = (phone: string): boolean => {
  // Support multiple formats: (123) 456-7890, 123-456-7890, 123.456.7890, 1234567890
  const phoneRegex = /^[+]?[\d\s\-().]{10,}$/;
  return phoneRegex.test(phone.replace(/\s/g, ''));
};

// Debounce function
export const debounce = <T extends (...args: any[]) => any>(
  func: T,
  wait: number
): ((...args: Parameters<T>) => void) => {
  let timeout: NodeJS.Timeout;
  return (...args: Parameters<T>) => {
    clearTimeout(timeout);
    timeout = setTimeout(() => func(...args), wait);
  };
};

// Throttle function
export const throttle = <T extends (...args: any[]) => any>(
  func: T,
  limit: number
): ((...args: Parameters<T>) => void) => {
  let inThrottle: boolean;
  return (...args: Parameters<T>) => {
    if (!inThrottle) {
      func(...args);
      inThrottle = true;
      setTimeout(() => (inThrottle = false), limit);
    }
  };
};

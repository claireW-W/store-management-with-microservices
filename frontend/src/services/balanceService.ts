// Balance service
import api from './api';
import { Balance } from '../types';
import { ApiError } from '../types';

class BalanceService {
  /**
   * Get user account balance
   * GET /api/auth/balance
   */
  async getBalance(): Promise<Balance> {
    try {
      const response = await api.get<Balance>('/auth/balance');
      return response.data;
    } catch (error: any) {
      const apiError: ApiError = {
        error: error.response?.data?.error || 'Network Error',
        message: error.response?.data?.message || error.message || 'Failed to get balance',
        requestId: error.response?.data?.requestId,
        timestamp: error.response?.data?.timestamp,
      };
      throw apiError;
    }
  }

  /**
   * Format balance for display
   */
  formatBalance(balance: number, currency: string = 'AUD'): string {
    return new Intl.NumberFormat('en-AU', {
      style: 'currency',
      currency: currency,
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(balance);
  }

  /**
   * Check if balance is sufficient for order amount
   */
  hasSufficientBalance(balance: number, orderAmount: number): boolean {
    return balance >= orderAmount;
  }
}

export const balanceService = new BalanceService();
export default balanceService;


// Inventory service
import api from './api';
import { Inventory } from '../types';

const WAREHOUSE_API_URL = '/warehouse'; // Endpoint in store-backend-service (or directly to warehouse-service)

export const inventoryService = {
  /**
   * Fetches inventory for a specific product across all warehouses.
   */
  async getInventoryByProduct(productId: number): Promise<Inventory[]> {
    try {
      // Call warehouse service directly
      // In production, this should go through nginx proxy or store-backend-service
      const warehouseServiceUrl = process.env.REACT_APP_WAREHOUSE_SERVICE_URL || 
        (typeof window !== 'undefined' && window.location.hostname === 'localhost' 
          ? 'http://localhost:8083/api' 
          : '/api/warehouse-service');
      
      const url = `${warehouseServiceUrl}/warehouse/inventory/${productId}`;
      
      const response = await fetch(url, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        if (response.status === 404) {
          // No inventory found
          return [];
        }
        throw new Error(`Failed to fetch inventory: ${response.statusText}`);
      }

      const data = await response.json();
      return Array.isArray(data) ? data : [];
    } catch (error) {
      console.error('Error fetching inventory:', error);
      // Return empty array on error to allow graceful degradation
      return [];
    }
  },

  /**
   * Calculates total available quantity across all warehouses for a product.
   */
  async getTotalAvailableQuantity(productId: number): Promise<number> {
    try {
      const inventories = await this.getInventoryByProduct(productId);
      return inventories.reduce((total, inv) => total + (inv.availableQuantity || 0), 0);
    } catch (error) {
      console.error('Error calculating total available quantity:', error);
      return 0;
    }
  },

  /**
   * Checks if there is sufficient inventory for a given quantity.
   */
  hasSufficientInventory(availableQuantity: number, requiredQuantity: number): boolean {
    return availableQuantity >= requiredQuantity;
  },

  /**
   * Formats inventory status message.
   */
  formatInventoryMessage(available: number, required: number, productName?: string): string {
    const shortfall = Math.max(0, required - available);
    if (shortfall > 0) {
      return `${productName || 'Product'} is out of stock. Available: ${available}, Required: ${required}, Shortfall: ${shortfall}`;
    }
    return `In stock: ${available} available`;
  },
};


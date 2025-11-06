// Product service
import api from './api';
import { Product } from '../types';

export const productService = {
  // Get all products
  async getProducts(): Promise<Product[]> {
    const response = await api.get<Product[]>('/products');
    return response.data;
  },

  // Get single product
  async getProductById(productId: number): Promise<Product> {
    const response = await api.get<Product>(`/products/${productId}`);
    return response.data;
  },
};

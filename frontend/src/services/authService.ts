// Authentication service
import api from './api';
import { LoginRequest, LoginResponse, SignupRequest, SignupResponse, User } from '../types';

export const authService = {
  // User login
  async login(credentials: LoginRequest): Promise<LoginResponse> {
    const response = await api.post<LoginResponse>('/auth/login', credentials);
    return response.data;
  },

  // User signup (create new user) - Admin operation
  async signup(userData: SignupRequest): Promise<SignupResponse> {
    const response = await api.post<SignupResponse>('/auth/signup', userData);
    return response.data;
  },

  // User logout
  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  },

  // Get current user information
  getCurrentUser(): User | null {
    const userStr = localStorage.getItem('user');
    if (userStr) {
      try {
        return JSON.parse(userStr);
      } catch {
        return null;
      }
    }
    return null;
  },

  // Check if user is authenticated
  isAuthenticated(): boolean {
    const token = localStorage.getItem('token');
    return !!token;
  },

  // Save user information and token
  saveAuthData(token: string, user: User): void {
    localStorage.setItem('token', token);
    localStorage.setItem('user', JSON.stringify(user));
  },
};

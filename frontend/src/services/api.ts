// API service layer - Axios instance configuration
import axios, { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse } from 'axios';
import { ApiError } from '../types';

// Create axios instance
// Use environment variable or detect Docker vs local
const getApiBaseURL = () => {
  // Environment variable takes priority (set during Docker build)
  // In production build with REACT_APP_API_URL set, use it directly
  if (process.env.REACT_APP_API_URL) {
    console.log('Using REACT_APP_API_URL:', process.env.REACT_APP_API_URL);
    return process.env.REACT_APP_API_URL;
  }
  // In browser, detect environment
  if (typeof window !== 'undefined') {
    // Check if we're in production build
    const isProduction = process.env.NODE_ENV === 'production';
    
    if (isProduction) {
      // Production build (Docker): always use relative path for nginx proxy
      console.log('Production build detected, using /api');
      return '/api';
    } else {
      // Development mode: connect directly to backend
      console.log('Development mode, using http://localhost:8080/api');
      return 'http://localhost:8080/api';
    }
  }
  // Fallback
  return 'http://localhost:8080/api';
};

const api: AxiosInstance = axios.create({
  baseURL: getApiBaseURL(),
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor - automatically add JWT token
api.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('token');
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor - unified error handling
api.interceptors.response.use(
  (response: AxiosResponse) => {
    return response;
  },
  (error) => {
    // Handle 401 unauthorized errors
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    
    // Unified error format
    const apiError: ApiError = {
      error: error.response?.data?.error || 'Network Error',
      message: error.response?.data?.message || error.message || 'An unexpected error occurred',
      requestId: error.response?.data?.requestId,
      timestamp: new Date().toISOString(),
    };
    
    return Promise.reject(apiError);
  }
);

export default api;

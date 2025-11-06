// WebSocket service for real-time notifications
import SockJS from 'sockjs-client';
import { Client, StompSubscription, IMessage, Frame } from '@stomp/stompjs';
import { PaymentNotification, DeliveryNotification } from '../types';

class WebSocketService {
  private client: Client | null = null;
  private subscriptions: Map<string, StompSubscription> = new Map();
  private isConnecting: boolean = false;
  private isConnected: boolean = false;

  connect(userId: number | string): Promise<void> {
    if (this.isConnected || this.isConnecting) {
      console.log('WebSocket already connected or connecting');
      return Promise.resolve();
    }

    this.isConnecting = true;

    return new Promise((resolve, reject) => {
      try {
        // Create SockJS connection
        // Note: Backend has context-path /api, so WebSocket endpoint is /api/ws
        const getWebSocketURL = () => {
          // Environment variable takes priority
          if (process.env.REACT_APP_WS_URL) {
            return process.env.REACT_APP_WS_URL;
          }
          // Detect environment
          if (typeof window !== 'undefined') {
            const isProduction = process.env.NODE_ENV === 'production';
            // Production build = Docker (use nginx proxy)
            // Development = local dev (direct connection)
            if (isProduction) {
              return '/api/ws';
            } else {
              return 'http://localhost:8080/api/ws';
            }
          }
          return 'http://localhost:8080/api/ws';
        };
        
        const socket = new SockJS(getWebSocketURL());
        
        // Create STOMP client
        this.client = new Client({
          webSocketFactory: () => socket as any,
          debug: (str: string) => {
            console.log('STOMP Debug:', str);
          },
          reconnectDelay: 5000,
          heartbeatIncoming: 4000,
          heartbeatOutgoing: 4000,
        });

        // Connection successful
        this.client.onConnect = () => {
          console.log('WebSocket connected successfully');
          this.isConnected = true;
          this.isConnecting = false;
          resolve();
        };

        // Connection error
        this.client.onStompError = (frame: Frame) => {
          console.error('STOMP error:', frame.headers['message']);
          console.error('Additional details:', frame.body);
          this.isConnecting = false;
          reject(new Error('WebSocket connection failed'));
        };

        // Activate the client
        this.client.activate();
      } catch (error) {
        console.error('Failed to create WebSocket connection:', error);
        this.isConnecting = false;
        reject(error);
      }
    });
  }

  /**
   * Unified subscription for order-related notifications
   * Backend sends to:
   *  - /user/{userId}/queue/orders (user-specific)
   *  - /topic/orders and /topic/orders/{orderNumber} (broadcast)
   */
  subscribeToOrderNotifications(
    userId: number | string,
    callback: (payload: any) => void
  ): void {
    if (!this.client || !this.isConnected) {
      console.error('WebSocket not connected. Cannot subscribe to order notifications.');
      return;
    }

    const destinations = [
      `/user/${userId}/queue/orders`,
      `/topic/orders`,
    ];

    destinations.forEach((destination) => {
      try {
        const subscription = this.client!.subscribe(destination, (message: IMessage) => {
          try {
            const data = JSON.parse(message.body);
            console.log('Received order notification:', destination, data);
            callback(data);
          } catch (error) {
            console.error('Failed to parse order notification:', error);
          }
        });
        this.subscriptions.set(`orders:${destination}`, subscription);
        console.log('Subscribed to order notifications:', destination);
      } catch (error) {
        console.error('Failed to subscribe to', destination, error);
      }
    });
  }

  subscribeToPaymentNotifications(
    userId: number | string,
    callback: (notification: PaymentNotification) => void
  ): void {
    if (!this.client || !this.isConnected) {
      console.error('WebSocket not connected. Cannot subscribe to payment notifications.');
      return;
    }

    const destination = `/user/${userId}/queue/payment`;
    
    try {
      const subscription = this.client.subscribe(destination, (message: IMessage) => {
        try {
          const notification: PaymentNotification = JSON.parse(message.body);
          console.log('Received payment notification:', notification);
          callback(notification);
        } catch (error) {
          console.error('Failed to parse payment notification:', error);
        }
      });

      this.subscriptions.set('payment', subscription);
      console.log('Subscribed to payment notifications:', destination);
    } catch (error) {
      console.error('Failed to subscribe to payment notifications:', error);
    }
  }

  subscribeToDeliveryNotifications(
    userId: number | string,
    callback: (notification: DeliveryNotification) => void
  ): void {
    if (!this.client || !this.isConnected) {
      console.error('WebSocket not connected. Cannot subscribe to delivery notifications.');
      return;
    }

    const destination = `/user/${userId}/queue/delivery`;
    
    try {
      const subscription = this.client.subscribe(destination, (message: IMessage) => {
        try {
          const notification: DeliveryNotification = JSON.parse(message.body);
          console.log('Received delivery notification:', notification);
          callback(notification);
        } catch (error) {
          console.error('Failed to parse delivery notification:', error);
        }
      });

      this.subscriptions.set('delivery', subscription);
      console.log('Subscribed to delivery notifications:', destination);
    } catch (error) {
      console.error('Failed to subscribe to delivery notifications:', error);
    }
  }

  disconnect(): void {
    if (this.client) {
      // Unsubscribe from all subscriptions
      this.subscriptions.forEach((subscription) => {
        subscription.unsubscribe();
      });
      this.subscriptions.clear();

      // Deactivate the client
      this.client.deactivate();
      this.isConnected = false;
      console.log('WebSocket disconnected');
    }
  }

  isWebSocketConnected(): boolean {
    return this.isConnected;
  }
}

// Export singleton instance
export const websocketService = new WebSocketService();




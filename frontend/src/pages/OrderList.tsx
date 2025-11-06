// Order list page component
import React, { useState, useEffect, useCallback, useRef } from 'react';
import OrderCard from '../components/orders/OrderCard';
import { OrderResponse } from '../types';
import { orderService } from '../services';
import { getErrorMessage, isActiveOrder } from '../utils/helpers';
import './OrderList.css';

const OrderList: React.FC = () => {
  const [orders, setOrders] = useState<OrderResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [pollingLoading, setPollingLoading] = useState(false);
  const intervalRef = useRef<NodeJS.Timeout | null>(null);
  const isMountedRef = useRef(true);
  const previousOrdersRef = useRef<string>('');
  
  // Polling state refs
  const shouldPollRef = useRef(false);
  const ordersRef = useRef<OrderResponse[]>([]);
  const errorRef = useRef<string | null>(null);
  const pollingStartedRef = useRef(false);
  
  // Helper to check if we should poll based on orders
  const checkShouldPoll = useCallback((ordersList: OrderResponse[], errorState: string | null): boolean => {
    const hasActiveOrders = ordersList.some(order => isActiveOrder(order.status));
    return hasActiveOrders && !errorState;
  }, []);

  // Fetch orders function - don't depend on orders to avoid infinite loop
  const fetchOrders = useCallback(async (isPolling = false) => {
    if (!isMountedRef.current) return;
    
    try {
      if (isPolling) {
        console.log('[OrderList] Polling orders...');
        setPollingLoading(true);
      } else {
        console.log('[OrderList] Fetching orders...');
      }
      
      const fetchedOrders = await orderService.getOrders();
      
      if (!isMountedRef.current) return;
      
      console.log('[OrderList] Successfully fetched orders:', fetchedOrders.length);
      
      // Only update if data actually changed
      const currentString = JSON.stringify(fetchedOrders);
      if (currentString !== previousOrdersRef.current) {
        previousOrdersRef.current = currentString;
        
        // Update orders using functional setState to avoid dependency
        setOrders(prevOrders => {
          const newOrders = fetchedOrders;
          
          // Check if we should poll after state update
          setTimeout(() => {
            const shouldPoll = checkShouldPoll(newOrders, errorRef.current);
            updatePollingState(shouldPoll);
          }, 0);
          
          return newOrders;
        });
      }
      
      setError(null);
    } catch (err) {
      if (!isMountedRef.current) return;
      
      const errorMessage = getErrorMessage(err);
      console.error('[OrderList] Error fetching orders:', errorMessage);
      
      // Only set error if it's not a polling request (to avoid interrupting UI)
      if (!isPolling) {
        setError(errorMessage);
        
        // Update polling state after error
        setTimeout(() => {
          const shouldPoll = checkShouldPoll(ordersRef.current, errorMessage);
          updatePollingState(shouldPoll);
        }, 0);
      }
    } finally {
      if (isMountedRef.current) {
        if (isPolling) {
          setPollingLoading(false);
        }
      }
    }
  }, [checkShouldPoll]); // Depend on helper only
  
  // Ref to store fetchOrders function to avoid circular dependency
  const fetchOrdersRef = useRef<((isPolling?: boolean) => Promise<void>) | null>(null);
  
  // Update ref when fetchOrders changes
  useEffect(() => {
    fetchOrdersRef.current = fetchOrders;
  }, [fetchOrders]);
  
  const updatePollingState = useCallback((shouldPoll: boolean) => {
    if (shouldPollRef.current === shouldPoll) return; // No change needed
    
    shouldPollRef.current = shouldPoll;
    
    // Clear existing interval
    if (intervalRef.current) {
      clearInterval(intervalRef.current);
      intervalRef.current = null;
      pollingStartedRef.current = false;
    }
    
    if (shouldPoll && !pollingStartedRef.current) {
      pollingStartedRef.current = true;
      
      // Start polling - wait for first interval before executing
      intervalRef.current = setInterval(() => {
        // Check condition before each poll
        const currentShouldPoll = checkShouldPoll(ordersRef.current, errorRef.current);
        
        if (currentShouldPoll && isMountedRef.current && fetchOrdersRef.current) {
          fetchOrdersRef.current(true);
        } else {
          // Stop polling
          if (intervalRef.current) {
            clearInterval(intervalRef.current);
            intervalRef.current = null;
            pollingStartedRef.current = false;
            shouldPollRef.current = false;
          }
        }
      }, 5000); // Poll every 5 seconds
      
      console.log('[OrderList] Started polling for active orders');
    } else if (!shouldPoll) {
      pollingStartedRef.current = false;
      console.log('[OrderList] Stopped polling (no active orders or error)');
    }
  }, [checkShouldPoll]);

  // Initial fetch
  useEffect(() => {
    const initialFetch = async () => {
      try {
        setLoading(true);
        const fetchedOrders = await orderService.getOrders();
        console.log('[OrderList] Initial fetch successful:', fetchedOrders.length);
        const ordersString = JSON.stringify(fetchedOrders);
        previousOrdersRef.current = ordersString;
        
        // Update refs
        ordersRef.current = fetchedOrders;
        errorRef.current = null;
        
        // Update state
        setOrders(fetchedOrders);
        setError(null);
        
        // Check and start polling if needed (after state update)
        setTimeout(() => {
          const shouldPoll = checkShouldPoll(fetchedOrders, null);
          updatePollingState(shouldPoll);
        }, 100);
      } catch (err) {
        const errorMessage = getErrorMessage(err);
        console.error('[OrderList] Initial fetch error:', errorMessage);
        errorRef.current = errorMessage;
        setError(errorMessage);
      } finally {
        setLoading(false);
      }
    };

    initialFetch();
  }, []); // Only run once on mount
  
  // Update refs when orders/error change (but don't trigger polling restart)
  useEffect(() => {
    ordersRef.current = orders;
    errorRef.current = error;
  }, [orders, error]);

  // Cleanup on unmount
  useEffect(() => {
    return () => {
      isMountedRef.current = false;
      if (intervalRef.current) {
        clearInterval(intervalRef.current);
        intervalRef.current = null;
      }
    };
  }, []);

  if (loading) {
    return (
      <div className="order-list-container">
        <div className="loading-container">
          <div className="loading-spinner"></div>
          <p>Loading your orders...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="order-list-container">
        <div className="error-container">
          <h2>Error Loading Orders</h2>
          <p>{error}</p>
          <button 
            className="retry-btn"
            onClick={async () => {
              console.log('[OrderList] User clicked retry');
              setError(null);
              try {
                const fetchedOrders = await orderService.getOrders();
                setOrders(fetchedOrders);
              } catch (err) {
                const errorMessage = getErrorMessage(err);
                setError(errorMessage);
              }
            }}
          >
            Retry
          </button>
        </div>
      </div>
    );
  }

  if (orders.length === 0) {
    return (
      <div className="order-list-container">
        <div className="empty-container">
          <h2>No Orders Yet</h2>
          <p>You haven't placed any orders yet. Start shopping to see your orders here!</p>
          <a href="/" className="shop-btn">
            Start Shopping
          </a>
        </div>
      </div>
    );
  }

  // Sort orders by creation date (newest first)
  const sortedOrders = [...orders].sort((a, b) => 
    new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
  );

  const activeOrdersCount = orders.filter(order => isActiveOrder(order.status)).length;

  return (
    <div className="order-list-container">
      <div className="order-list-header">
        <h1>My Orders</h1>
        <div className="order-stats">
          <div className="stat-item">
            <span className="stat-label">Total Orders:</span>
            <span className="stat-value">{orders.length}</span>
          </div>
          {activeOrdersCount > 0 && (
            <div className="stat-item">
              <span className="stat-label">Active Orders:</span>
              <span className="stat-value active">{activeOrdersCount}</span>
            </div>
          )}
          {pollingLoading && (
            <div className="polling-indicator">
              <div className="polling-dot"></div>
              <span>Updating...</span>
            </div>
          )}
        </div>
      </div>

      <div className="order-grid">
        {sortedOrders.map((order) => (
          <OrderCard key={order.id} order={order} />
        ))}
      </div>
    </div>
  );
};

export default OrderList;

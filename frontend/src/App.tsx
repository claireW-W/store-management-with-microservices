import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './components/Login';
import Layout from './components/layout/Layout';
import ProtectedRoute from './components/auth/ProtectedRoute';
import ProductList from './pages/ProductList';
import OrderList from './pages/OrderList';
import OrderDetails from './pages/OrderDetails';
import Toast from './components/common/Toast';
import { authService } from './services/authService';
import { websocketService } from './services/websocketService';
import { balanceService } from './services/balanceService';
import { useToast } from './hooks/useToast';
import { User, Balance } from './types';
import './App.css';

function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [user, setUser] = useState<User | null>(null);
  const [balance, setBalance] = useState<Balance | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const { toasts, showToast, removeToast } = useToast();

  // Check authentication status on app load
  useEffect(() => {
    const checkAuth = () => {
      const isAuthenticated = authService.isAuthenticated();
      const currentUser = authService.getCurrentUser();
      
      setIsLoggedIn(isAuthenticated);
      setUser(currentUser);
      setIsLoading(false);
    };

    checkAuth();
  }, []);

  // Fetch balance when user is logged in
  useEffect(() => {
    if (isLoggedIn && user) {
      const fetchBalance = async () => {
        try {
          const balanceData = await balanceService.getBalance();
          setBalance(balanceData);
        } catch (error) {
          console.error('Failed to fetch balance:', error);
          // Don't show error toast for balance - it's not critical
        }
      };
      
      fetchBalance();
      
      // Refresh balance periodically (every 30 seconds)
      const balanceInterval = setInterval(fetchBalance, 30000);
      
      return () => {
        clearInterval(balanceInterval);
      };
    } else {
      setBalance(null);
    }
  }, [isLoggedIn, user]);

  // Initialize WebSocket connection when user is logged in
  useEffect(() => {
    if (isLoggedIn && user) {
      console.log('Initializing WebSocket connection for user:', user.id);
      
      websocketService.connect(user.id)
        .then(() => {
          console.log('WebSocket connected successfully');
          
          // Unified order notifications (payment + delivery + order updates)
          websocketService.subscribeToOrderNotifications(
            user.id,
            (data: any) => {
              // Normalize payload fields
              const orderNumber = data.orderNumber || data.orderId || 'UNKNOWN';
              const status = (data.newStatus || data.status || '').toString();
              const message = data.message || 'Order update';

              let type: 'success' | 'error' | 'info' | 'warning' = 'info';
              if (status === 'DELIVERED' || message.includes('Payment successful')) type = 'success';
              if (status === 'LOST' || status === 'FAILED') type = 'error';

              showToast(`${message} (Order #${orderNumber})`, type);
              
              // Refresh balance after payment notifications
              if (message.includes('Payment') || message.includes('Refund')) {
                balanceService.getBalance()
                  .then(setBalance)
                  .catch(console.error);
              }
            }
          );
          
          showToast('Real-time notifications enabled', 'info');
        })
        .catch((error) => {
          console.error('Failed to connect WebSocket:', error);
          showToast('⚠️ Failed to connect real-time notifications', 'warning');
        });
      
      // Cleanup on logout
      return () => {
        console.log('Disconnecting WebSocket');
        websocketService.disconnect();
      };
    }
  }, [isLoggedIn, user, showToast]);

  const handleLogin = (loginToken: string, userData: User) => {
    // Store token and user data
    authService.saveAuthData(loginToken, userData);
    setUser(userData);
    setIsLoggedIn(true);
  };

  const handleLogout = () => {
    authService.logout();
    setUser(null);
    setIsLoggedIn(false);
  };

  if (isLoading) {
    return (
      <div className="App">
        <div className="loading-container">
          <div className="loading-spinner"></div>
          <p>Loading...</p>
        </div>
      </div>
    );
  }

  return (
    <Router>
      <div className="App">
        {/* Toast notifications */}
        <div className="toast-container">
          {toasts.map((toast) => (
            <Toast
              key={toast.id}
              message={toast.message}
              type={toast.type}
              onClose={() => removeToast(toast.id)}
            />
          ))}
        </div>
        
        <Routes>
          {/* Public routes */}
          <Route 
            path="/login" 
            element={
              isLoggedIn ? (
                <Navigate to="/" replace />
              ) : (
                <Login onLogin={handleLogin} />
              )
            } 
          />
          
          {/* Protected routes */}
          <Route
            path="/*"
            element={
              <ProtectedRoute>
                <Layout 
                  user={user} 
                  balance={balance?.balance ?? null}
                  currency={balance?.currency}
                  onLogout={handleLogout}
                >
                  <Routes>
                    <Route path="/" element={<ProductList balance={balance} />} />
                    <Route path="/orders" element={<OrderList />} />
                    <Route path="/orders/:id" element={<OrderDetails />} />
                    <Route path="*" element={<Navigate to="/" replace />} />
                  </Routes>
                </Layout>
              </ProtectedRoute>
            }
          />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
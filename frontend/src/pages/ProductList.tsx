// Product list page component
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import ProductCard from '../components/products/ProductCard';
import OrderFormModal from '../components/products/OrderFormModal';
import { Product, CreateOrderRequest, Balance } from '../types';
import { productService, orderService, balanceService } from '../services';
import { getErrorMessage } from '../utils/helpers';
import './ProductList.css';

interface ProductListProps {
  balance?: Balance | null;
}

const ProductList: React.FC<ProductListProps> = ({ balance }) => {
  const navigate = useNavigate();
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null);
  const [isOrderModalOpen, setIsOrderModalOpen] = useState(false);
  const [isOrdering, setIsOrdering] = useState(false);

  // Fetch products on component mount
  useEffect(() => {
    const fetchProducts = async () => {
      try {
        setLoading(true);
        setError(null);
        const fetchedProducts = await productService.getProducts();
        setProducts(fetchedProducts);
      } catch (err) {
        setError(getErrorMessage(err));
      } finally {
        setLoading(false);
      }
    };

    fetchProducts();
  }, []);

  const handleOrderClick = (product: Product) => {
    setSelectedProduct(product);
    setIsOrderModalOpen(true);
  };

  const handleOrderSubmit = async (orderData: CreateOrderRequest) => {
    try {
      setIsOrdering(true);
      
      // Calculate total order amount
      const totalAmount = orderData.items.reduce((sum, item) => {
        const product = products.find(p => p.id === item.productId);
        return sum + (product ? product.price * item.quantity : 0);
      }, 0);
      
      // Check balance before submitting order
      let currentBalance = balance?.balance;
      
      // Refresh balance if not available
      if (currentBalance === null || currentBalance === undefined) {
        try {
          const balanceData = await balanceService.getBalance();
          currentBalance = balanceData.balance;
        } catch (error) {
          console.error('Failed to fetch balance:', error);
        }
      }
      
      // Check if balance is sufficient
      if (currentBalance !== null && currentBalance !== undefined) {
        if (!balanceService.hasSufficientBalance(currentBalance, totalAmount)) {
          const formattedBalance = balanceService.formatBalance(currentBalance, balance?.currency);
          const formattedAmount = balanceService.formatBalance(totalAmount, balance?.currency);
          const shortfall = balanceService.formatBalance(totalAmount - currentBalance, balance?.currency);
          
          alert(`❌ Insufficient Balance!\n\n` +
                `Your balance: ${formattedBalance}\n` +
                `Order total: ${formattedAmount}\n` +
                `Shortfall: ${shortfall}\n\n` +
                `Please add funds to your account before placing this order.`);
          throw new Error('Insufficient balance');
        }
      }
      
      // Show processing message
      const processingMessage = document.createElement('div');
      processingMessage.className = 'payment-processing-overlay';
      processingMessage.innerHTML = `
        <div class="payment-processing-content">
          <div class="payment-spinner"></div>
          <h3>Processing Payment...</h3>
          <p>Please wait while we process your payment</p>
          <small>🔒 Secure payment via Bank Service</small>
        </div>
      `;
      document.body.appendChild(processingMessage);
      
      // Create order (this will trigger payment processing in backend)
      const order = await orderService.createOrder(orderData);
      
      // Remove processing overlay
      document.body.removeChild(processingMessage);
      
      // Show success message
      alert(`✅ Order placed successfully! Order #${order.orderNumber}\n\n` +
            `💡 You will receive real-time notifications for:\n` +
            `• Payment confirmation\n` +
            `• Delivery status updates`);
      
      // Navigate to orders page
      navigate('/orders');
    } catch (err) {
      // Remove processing overlay if it exists
      const overlay = document.querySelector('.payment-processing-overlay');
      if (overlay) {
        document.body.removeChild(overlay);
      }
      
      const errorMessage = getErrorMessage(err);
      
      // Parse and format error messages for better user experience
      let userFriendlyMessage = errorMessage;
      
      // Handle inventory-related errors
      if (errorMessage.includes('Inventory') || errorMessage.includes('inventory') || 
          errorMessage.includes('stock') || errorMessage.includes('Stock') ||
          errorMessage.includes('Insufficient inventory')) {
        // Extract product ID and quantity from error message if available
        const inventoryMatch = errorMessage.match(/product[:\s]+(\d+).*Required[:\s]+(\d+)/i);
        if (inventoryMatch) {
          const productId = inventoryMatch[1];
          const requiredQuantity = inventoryMatch[2];
          const product = products.find(p => p.id.toString() === productId);
          userFriendlyMessage = `❌ Insufficient Stock!\n\n` +
                              `Product: ${product ? product.name : `Product #${productId}`}\n` +
                              `Requested quantity: ${requiredQuantity}\n` +
                              `Unfortunately, we don't have enough stock for this item.\n\n` +
                              `Please reduce the quantity or try again later.`;
        } else {
          userFriendlyMessage = `❌ Insufficient Stock!\n\n` +
                              `Unfortunately, we don't have enough stock for this item.\n\n` +
                              `Please reduce the quantity or try again later.`;
        }
      }
      // Handle balance-related errors (but don't show alert as it's already shown above)
      else if (errorMessage.includes('Insufficient balance') || errorMessage.includes('balance')) {
        // Already shown above, don't show again
        throw err;
      }
      // Handle other errors
      else {
        userFriendlyMessage = `❌ Failed to place order\n\n${errorMessage}\n\nPlease try again.`;
      }
      
      // Show error alert
      alert(userFriendlyMessage);
      
      throw err; // Re-throw to prevent modal from closing
    } finally {
      setIsOrdering(false);
    }
  };

  const handleCloseModal = () => {
    setIsOrderModalOpen(false);
    setSelectedProduct(null);
  };

  if (loading) {
    return (
      <div className="product-list-container">
        <div className="loading-container">
          <div className="loading-spinner"></div>
          <p>Loading products...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="product-list-container">
        <div className="error-container">
          <h2>Error Loading Products</h2>
          <p>{error}</p>
          <button 
            className="retry-btn"
            onClick={() => window.location.reload()}
          >
            Retry
          </button>
        </div>
      </div>
    );
  }

  if (products.length === 0) {
    return (
      <div className="product-list-container">
        <div className="empty-container">
          <h2>No Products Available</h2>
          <p>There are currently no products available for purchase.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="product-list-container">
      <div className="product-list-header">
        <h1>Our Products</h1>
        <p>Choose from our selection of high-quality products</p>
      </div>

      <div className="product-grid">
        {products.map((product) => (
          <ProductCard
            key={product.id}
            product={product}
            onOrderClick={handleOrderClick}
          />
        ))}
      </div>

      <OrderFormModal
        product={selectedProduct}
        isOpen={isOrderModalOpen}
        onClose={handleCloseModal}
        onSubmit={handleOrderSubmit}
        isLoading={isOrdering}
        balance={balance}
      />
    </div>
  );
};

export default ProductList;

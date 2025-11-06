// Product card component
import React from 'react';
import { Product } from '../../types';
import { formatCurrency } from '../../utils/helpers';
import './ProductCard.css';

interface ProductCardProps {
  product: Product;
  onOrderClick: (product: Product) => void;
}

const ProductCard: React.FC<ProductCardProps> = ({ product, onOrderClick }) => {
  return (
    <div className="product-card">
      <div className="product-image">
        <div className="product-image-placeholder">
          <span className="product-image-icon">📦</span>
        </div>
      </div>
      
      <div className="product-content">
        <h3 className="product-name">{product.name}</h3>
        <p className="product-description">{product.description}</p>
        
        <div className="product-details">
          <div className="product-price">
            {formatCurrency(product.price)}
          </div>
          <div className="product-sku">
            SKU: {product.sku}
          </div>
        </div>
        
        <button 
          className="product-order-btn"
          onClick={() => onOrderClick(product)}
        >
          Order Now
        </button>
      </div>
    </div>
  );
};

export default ProductCard;

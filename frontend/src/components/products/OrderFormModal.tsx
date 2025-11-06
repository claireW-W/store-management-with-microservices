// Order form modal component
import React, { useState, useMemo, useEffect } from 'react';
import { Product, CreateOrderRequest, Balance, Inventory } from '../../types';
import { formatCurrency, isValidPostalCode } from '../../utils/helpers';
import { balanceService, inventoryService } from '../../services';
import { useFormValidation } from '../../hooks';
import './OrderFormModal.css';

interface OrderFormModalProps {
  product: Product | null;
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (orderData: CreateOrderRequest) => Promise<void>;
  isLoading?: boolean;
  balance?: Balance | null;
}

const OrderFormModal: React.FC<OrderFormModalProps> = ({
  product,
  isOpen,
  onClose,
  onSubmit,
  isLoading = false,
  balance
}) => {
  const [quantity, setQuantity] = useState(1);
  const [inventory, setInventory] = useState<Inventory[]>([]);
  const [inventoryLoading, setInventoryLoading] = useState(false);
  const [inventoryError, setInventoryError] = useState<string | null>(null);
  
  // Fetch inventory when product changes
  useEffect(() => {
    if (product && isOpen) {
      setInventoryLoading(true);
      setInventoryError(null);
      inventoryService.getInventoryByProduct(product.id)
        .then((inventories) => {
          setInventory(inventories);
          setInventoryLoading(false);
        })
        .catch((error) => {
          console.error('Failed to fetch inventory:', error);
          setInventoryError('Unable to check inventory');
          setInventoryLoading(false);
          setInventory([]);
        });
    } else {
      setInventory([]);
      setInventoryError(null);
    }
  }, [product, isOpen]);
  
  // Calculate total available quantity across all warehouses
  const totalAvailableQuantity = useMemo(() => {
    return inventory.reduce((total, inv) => total + (inv.availableQuantity || 0), 0);
  }, [inventory]);
  
  // Check if inventory is sufficient
  const hasSufficientInventory = useMemo(() => {
    if (inventoryLoading || inventoryError) {
      return true; // Allow if inventory not loaded yet (graceful degradation)
    }
    return inventoryService.hasSufficientInventory(totalAvailableQuantity, quantity);
  }, [totalAvailableQuantity, quantity, inventoryLoading, inventoryError]);
  
  const inventoryShortfall = useMemo(() => {
    return Math.max(0, quantity - totalAvailableQuantity);
  }, [quantity, totalAvailableQuantity]);
  
  // Calculate total price and check balance
  const totalPrice = useMemo(() => {
    return product ? product.price * quantity : 0;
  }, [product, quantity]);
  
  const hasSufficientBalance = useMemo(() => {
    if (!balance || balance.balance === null || balance.balance === undefined) {
      return true; // Allow if balance not loaded yet
    }
    return balanceService.hasSufficientBalance(balance.balance, totalPrice);
  }, [balance, totalPrice]);
  
  const balanceShortfall = useMemo(() => {
    if (!balance || balance.balance === null || balance.balance === undefined) {
      return 0;
    }
    return Math.max(0, totalPrice - balance.balance);
  }, [balance, totalPrice]);

  const {
    values,
    errors,
    touched,
    setValue,
    setFieldTouched,
    validateAll,
    reset
  } = useFormValidation({
    street: '',
    suburb: '',
    state: '',
    postcode: '',
    country: 'Australia',
    paymentMethod: 'credit_card',
    notes: ''
  }, {
    street: (value) => !value ? 'Please fill in Street Address' : null,
    suburb: (value) => !value ? 'Please fill in Suburb' : null,
    state: (value) => !value ? 'Please fill in State' : null,
    postcode: (value) => !value ? 'Please fill in Postcode' : 
      !isValidPostalCode(value) ? 'Postcode must be 4 digits' : null,
    country: (value) => !value ? 'Please fill in Country' : null,
    paymentMethod: (value) => !value ? 'Please fill in Payment Method' : null
  });

  const handleClose = () => {
    setQuantity(1);
    reset();
    onClose();
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!product || !validateAll()) {
      return;
    }

    const orderData: CreateOrderRequest = {
      items: [{
        productId: product.id,
        quantity: quantity
      }],
      shippingAddress: {
        street: values.street,
        suburb: values.suburb,
        state: values.state,
        postcode: values.postcode,
        country: values.country
      },
      paymentMethod: values.paymentMethod,
      notes: values.notes || undefined
    };

    try {
      await onSubmit(orderData);
      handleClose();
    } catch (error) {
      // Error handling is done in parent component
    }
  };

  if (!isOpen || !product) {
    return null;
  }

  return (
    <div className="modal-overlay" onClick={handleClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>Place Order</h2>
          <button className="modal-close-btn" onClick={handleClose}>
            ×
          </button>
        </div>

        <div className="modal-body">
          {/* Product Summary */}
          <div className="product-summary">
            <h3>{product.name}</h3>
            <div className="product-summary-details">
              <div className="quantity-selector">
                <label htmlFor="quantity">Quantity:</label>
                <select
                  id="quantity"
                  value={quantity}
                  onChange={(e) => setQuantity(parseInt(e.target.value))}
                >
                  {Array.from({ length: 10 }, (_, i) => i + 1).map(num => (
                    <option key={num} value={num}>{num}</option>
                  ))}
                </select>
              </div>
              <div className="price-breakdown">
                <div className="unit-price">
                  Unit Price: {formatCurrency(product.price)}
                </div>
                <div className="total-price-wrapper">
                  <span className="total-price-label">Total:</span>
                  <span className="total-price">{formatCurrency(totalPrice)}</span>
                </div>
              </div>
              
              {/* Inventory information */}
              {inventoryLoading ? (
                <div className="inventory-info">
                  <div className="inventory-loading">
                    <span>Checking inventory...</span>
                  </div>
                </div>
              ) : inventoryError ? (
                <div className="inventory-info">
                  <div className="inventory-error">
                    <span className="error-icon">⚠️</span>
                    <span className="error-text">{inventoryError}</span>
                  </div>
                </div>
              ) : inventory.length > 0 ? (
                <div className="inventory-info">
                  <div className={`inventory-display ${hasSufficientInventory ? 'inventory-sufficient' : 'inventory-insufficient'}`}>
                    <span className="inventory-label">Available Stock:</span>
                    <span className="inventory-amount">
                      {totalAvailableQuantity} {totalAvailableQuantity === 1 ? 'unit' : 'units'}
                    </span>
                  </div>
                  {!hasSufficientInventory && (
                    <div className="inventory-warning">
                      <span className="warning-icon">⚠️</span>
                      <span className="warning-text">
                        Insufficient stock! Only {totalAvailableQuantity} {totalAvailableQuantity === 1 ? 'unit' : 'units'} available.
                      </span>
                    </div>
                  )}
                </div>
              ) : null}
              
              {/* Balance information */}
              {balance && balance.balance !== null && balance.balance !== undefined && (
                <div className="balance-info">
                  <div className={`balance-display ${hasSufficientBalance ? 'balance-sufficient' : 'balance-insufficient'}`}>
                    <span className="balance-label">Your Balance:</span>
                    <span className="balance-amount">
                      {balanceService.formatBalance(balance.balance, balance.currency)}
                    </span>
                  </div>
                  {!hasSufficientBalance && (
                    <div className="balance-warning">
                      <span className="warning-icon">⚠️</span>
                      <span className="warning-text">
                        Insufficient balance! You need {balanceService.formatBalance(balanceShortfall, balance.currency)} more.
                      </span>
                    </div>
                  )}
                </div>
              )}
            </div>
          </div>

          {/* Order Form */}
          <form onSubmit={handleSubmit} className="order-form">
            <div className="form-section">
              <h4>Shipping Address</h4>
              
              <div className="form-group">
                <label htmlFor="street">Street Address *</label>
                <input
                  type="text"
                  id="street"
                  value={values.street}
                  onChange={(e) => setValue('street', e.target.value)}
                  onBlur={() => setFieldTouched('street')}
                  className={errors.street && touched.street ? 'error' : ''}
                />
                {errors.street && touched.street && (
                  <span className="error-message">{errors.street}</span>
                )}
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label htmlFor="suburb">Suburb *</label>
                  <input
                    type="text"
                    id="suburb"
                    value={values.suburb}
                    onChange={(e) => setValue('suburb', e.target.value)}
                    onBlur={() => setFieldTouched('suburb')}
                    className={errors.suburb && touched.suburb ? 'error' : ''}
                  />
                  {errors.suburb && touched.suburb && (
                    <span className="error-message">{errors.suburb}</span>
                  )}
                </div>

                <div className="form-group">
                  <label htmlFor="state">State *</label>
                  <input
                    type="text"
                    id="state"
                    value={values.state}
                    onChange={(e) => setValue('state', e.target.value)}
                    onBlur={() => setFieldTouched('state')}
                    className={errors.state && touched.state ? 'error' : ''}
                  />
                  {errors.state && touched.state && (
                    <span className="error-message">{errors.state}</span>
                  )}
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label htmlFor="postcode">Postcode *</label>
                  <input
                    type="text"
                    id="postcode"
                    value={values.postcode}
                    onChange={(e) => setValue('postcode', e.target.value)}
                    onBlur={() => setFieldTouched('postcode')}
                    className={errors.postcode && touched.postcode ? 'error' : ''}
                    placeholder="1234"
                    maxLength={4}
                  />
                  {errors.postcode && touched.postcode && (
                    <span className="error-message">{errors.postcode}</span>
                  )}
                </div>

                <div className="form-group">
                  <label htmlFor="country">Country *</label>
                  <select
                    id="country"
                    value={values.country}
                    onChange={(e) => setValue('country', e.target.value)}
                    onBlur={() => setFieldTouched('country')}
                    className={errors.country && touched.country ? 'error' : ''}
                  >
                    <option value="Australia">Australia</option>
                    <option value="United States">United States</option>
                  </select>
                  {errors.country && touched.country && (
                    <span className="error-message">{errors.country}</span>
                  )}
                </div>
              </div>
            </div>

            <div className="form-section">
              <h4>Payment Information</h4>
              
              <div className="form-group">
                <label htmlFor="paymentMethod">Payment Method *</label>
                <select
                  id="paymentMethod"
                  value={values.paymentMethod}
                  onChange={(e) => setValue('paymentMethod', e.target.value)}
                  onBlur={() => setFieldTouched('paymentMethod')}
                  className={errors.paymentMethod && touched.paymentMethod ? 'error' : ''}
                >
                  <option value="credit_card">Credit Card</option>
                  <option value="debit_card">Debit Card</option>
                  <option value="paypal">PayPal</option>
                  <option value="bank_transfer">Bank Transfer</option>
                </select>
                {errors.paymentMethod && touched.paymentMethod && (
                  <span className="error-message">{errors.paymentMethod}</span>
                )}
              </div>
            </div>

            <div className="form-section">
              <h4>Additional Information</h4>
              
              <div className="form-group">
                <label htmlFor="notes">Notes (Optional)</label>
                <textarea
                  id="notes"
                  value={values.notes}
                  onChange={(e) => setValue('notes', e.target.value)}
                  rows={3}
                  placeholder="Any special instructions or notes..."
                />
              </div>
            </div>

            <div className="modal-footer">
              <button
                type="button"
                className="btn-secondary"
                onClick={handleClose}
                disabled={isLoading}
              >
                Cancel
              </button>
              <button
                type="submit"
                className={`btn-primary ${(!hasSufficientInventory || !hasSufficientBalance) ? 'btn-disabled' : ''}`}
                disabled={
                  isLoading || 
                  (!hasSufficientInventory && !inventoryLoading && !inventoryError) ||
                  (balance !== null && balance !== undefined && balance.balance !== null && balance.balance !== undefined && !hasSufficientBalance)
                }
                title={
                  !hasSufficientInventory && !inventoryLoading && !inventoryError 
                    ? `Insufficient stock. Only ${totalAvailableQuantity} available.` 
                    : !hasSufficientBalance && balance 
                      ? 'Insufficient balance to place order' 
                      : ''
                }
              >
                {isLoading ? 'Placing Order...' : 
                 !hasSufficientInventory && !inventoryLoading && !inventoryError
                   ? `Insufficient Stock - ${totalAvailableQuantity} available`
                   : !hasSufficientBalance && balance && balance.balance !== null ? 
                     `Insufficient Balance - ${formatCurrency(totalPrice)}` :
                     `Place Order - ${formatCurrency(totalPrice)}`}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default OrderFormModal;

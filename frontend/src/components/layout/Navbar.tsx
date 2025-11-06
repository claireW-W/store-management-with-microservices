// Navigation bar component
import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import './Navbar.css';

interface NavbarProps {
  user: {
    username: string;
    firstName: string;
    lastName: string;
  } | null;
  balance?: number | null;
  currency?: string;
  onLogout: () => void;
}

const Navbar: React.FC<NavbarProps> = ({ user, balance, currency = 'AUD', onLogout }) => {
  const navigate = useNavigate();

  const handleLogout = () => {
    onLogout();
    navigate('/login');
  };

  const formatBalance = (amount: number | null | undefined): string => {
    if (amount === null || amount === undefined) {
      return 'Loading...';
    }
    return new Intl.NumberFormat('en-AU', {
      style: 'currency',
      currency: currency,
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(amount);
  };

  return (
    <nav className="navbar">
      <div className="navbar-container">
        <div className="navbar-brand">
          <Link to="/" className="navbar-logo">
            Store
          </Link>
        </div>
        
        <div className="navbar-menu">
          <div className="navbar-nav">
            <Link to="/" className="navbar-link">
              Products
            </Link>
            <Link to="/orders" className="navbar-link">
              My Orders
            </Link>
          </div>
          
          <div className="navbar-user">
            {balance !== null && balance !== undefined && (
              <span className="navbar-balance">
                Balance: <strong className={balance >= 0 ? 'balance-positive' : 'balance-negative'}>
                  {formatBalance(balance)}
                </strong>
              </span>
            )}
            {user && (
              <span className="navbar-welcome">
                Welcome, {user.firstName} {user.lastName}
              </span>
            )}
            <button 
              className="navbar-logout-btn"
              onClick={handleLogout}
            >
              Logout
            </button>
          </div>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;

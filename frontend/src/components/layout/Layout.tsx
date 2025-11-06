// Layout component for authenticated pages
import React from 'react';
import Navbar from './Navbar';
import './Layout.css';

interface LayoutProps {
  children: React.ReactNode;
  user: {
    username: string;
    firstName: string;
    lastName: string;
  } | null;
  balance?: number | null;
  currency?: string;
  onLogout: () => void;
}

const Layout: React.FC<LayoutProps> = ({ children, user, balance, currency, onLogout }) => {
  return (
    <div className="layout">
      <Navbar user={user} balance={balance} currency={currency} onLogout={onLogout} />
      <main className="layout-main">
        <div className="layout-content">
          {children}
        </div>
      </main>
    </div>
  );
};

export default Layout;

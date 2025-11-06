import React, { useState } from 'react';
import { authService } from '../services/authService';
import { getErrorMessage } from '../utils/helpers';
import './Login.css';

interface LoginProps {
  onLogin: (token: string, user: any) => void;
}

const Login: React.FC<LoginProps> = ({ onLogin }) => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [showSignup, setShowSignup] = useState(false);
  const [signupData, setSignupData] = useState({
    username: '',
    password: '',
    email: '',
    firstName: '',
    lastName: '',
    phone: ''
  });
  const [signupLoading, setSignupLoading] = useState(false);
  const [signupError, setSignupError] = useState('');
  const [signupSuccess, setSignupSuccess] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const response = await authService.login({ username, password });

      if (response.token) {
        onLogin(response.token, {
          username: response.username,
          email: response.email,
          firstName: response.firstName,
          lastName: response.lastName
        });
      }
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  const handleSignup = async (e: React.FormEvent) => {
    e.preventDefault();
    setSignupLoading(true);
    setSignupError('');
    setSignupSuccess('');

    try {
      const response = await authService.signup(signupData);
      if (response.success) {
        setSignupSuccess(`User "${response.username}" created successfully!`);
        // Reset form
        setSignupData({
          username: '',
          password: '',
          email: '',
          firstName: '',
          lastName: '',
          phone: ''
        });
        // Switch back to login after 2 seconds
        setTimeout(() => {
          setShowSignup(false);
          setSignupSuccess('');
        }, 2000);
      }
    } catch (err) {
      setSignupError(getErrorMessage(err));
    } finally {
      setSignupLoading(false);
    }
  };

  return (
    <div className="login-container">
      <div className="login-card">
        {!showSignup ? (
          <>
            <h1>Store Login</h1>
            <p className="login-subtitle">Welcome to our online store</p>
            
            <form onSubmit={handleSubmit} className="login-form">
              <div className="form-group">
                <label htmlFor="username">Username</label>
                <input
                  type="text"
                  id="username"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  placeholder="Enter your username"
                  required
                />
              </div>
              
              <div className="form-group">
                <label htmlFor="password">Password</label>
                <input
                  type="password"
                  id="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="Enter your password"
                  required
                />
              </div>
              
              {error && <div className="error-message">{error}</div>}
              
              <button type="submit" disabled={loading} className="login-button">
                {loading ? 'Logging in...' : 'Login'}
              </button>
            </form>

            <div className="signup-link">
              <p>
                Need to create a user?{' '}
                <button 
                  type="button"
                  className="link-button"
                  onClick={() => setShowSignup(true)}
                >
                  Create User
                </button>
              </p>
            </div>
          </>
        ) : (
          <>
            <h1>Create User</h1>
            <p className="login-subtitle">Create a new user account</p>
            
            <form onSubmit={handleSignup} className="login-form">
              <div className="form-group">
                <label htmlFor="signup-username">Username *</label>
                <input
                  type="text"
                  id="signup-username"
                  value={signupData.username}
                  onChange={(e) => setSignupData({...signupData, username: e.target.value})}
                  placeholder="Enter username (min 3 characters)"
                  required
                  minLength={3}
                />
              </div>

              <div className="form-group">
                <label htmlFor="signup-password">Password *</label>
                <input
                  type="password"
                  id="signup-password"
                  value={signupData.password}
                  onChange={(e) => setSignupData({...signupData, password: e.target.value})}
                  placeholder="Enter password (min 6 characters)"
                  required
                  minLength={6}
                />
              </div>

              <div className="form-group">
                <label htmlFor="signup-email">Email *</label>
                <input
                  type="email"
                  id="signup-email"
                  value={signupData.email}
                  onChange={(e) => setSignupData({...signupData, email: e.target.value})}
                  placeholder="Enter email address"
                  required
                />
              </div>

              <div className="form-group">
                <label htmlFor="signup-firstname">First Name</label>
                <input
                  type="text"
                  id="signup-firstname"
                  value={signupData.firstName}
                  onChange={(e) => setSignupData({...signupData, firstName: e.target.value})}
                  placeholder="Enter first name"
                />
              </div>

              <div className="form-group">
                <label htmlFor="signup-lastname">Last Name</label>
                <input
                  type="text"
                  id="signup-lastname"
                  value={signupData.lastName}
                  onChange={(e) => setSignupData({...signupData, lastName: e.target.value})}
                  placeholder="Enter last name"
                />
              </div>

              <div className="form-group">
                <label htmlFor="signup-phone">Phone</label>
                <input
                  type="tel"
                  id="signup-phone"
                  value={signupData.phone}
                  onChange={(e) => setSignupData({...signupData, phone: e.target.value})}
                  placeholder="Enter phone number"
                />
              </div>
              
              {signupError && <div className="error-message">{signupError}</div>}
              {signupSuccess && <div className="success-message">{signupSuccess}</div>}
              
              <button type="submit" disabled={signupLoading} className="login-button">
                {signupLoading ? 'Creating...' : 'Create User'}
              </button>
            </form>

            <div className="signup-link">
              <p>
                Already have an account?{' '}
                <button 
                  type="button"
                  className="link-button"
                  onClick={() => {
                    setShowSignup(false);
                    setSignupError('');
                    setSignupSuccess('');
                  }}
                >
                  Back to Login
                </button>
              </p>
            </div>
          </>
        )}
      </div>
    </div>
  );
};

export default Login;

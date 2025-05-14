import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Header from './components/Header';
import LoginPage from './components/LoginPage';
import RegisterPage from './components/RegisterPage';
import ProductTable from './components/ProductTable';
import OrdersTable from './components/OrdersTable'; // Добавьте этот импорт
import InvoicesPage from './components/InvoicesPage';

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [authChecked, setAuthChecked] = useState(false);

  useEffect(() => {
    const checkAuth = () => {
      const token = localStorage.getItem('token');
      setIsAuthenticated(!!token);
      setAuthChecked(true);
    };
    
    checkAuth();
    
    window.addEventListener('storage', checkAuth);
    
    return () => {
      window.removeEventListener('storage', checkAuth);
    };
  }, []);

  if (!authChecked) {
    return <div className="loading-spinner">Проверка авторизации...</div>;
  }

  return (
    <Router>
      <div className="App">
        {isAuthenticated && <Header />}
        <Routes>
          <Route 
            path="/" 
            element={<Navigate to={isAuthenticated ? "/products" : "/login"} replace />} 
          />
          <Route 
            path="/login" 
            element={isAuthenticated ? <Navigate to="/products" replace /> : <LoginPage />} 
          />
          <Route 
            path="/register" 
            element={isAuthenticated ? <Navigate to="/products" replace /> : <RegisterPage />} 
          />
          <Route 
            path="/products" 
            element={isAuthenticated ? <ProductTable /> : <Navigate to="/login" replace />} 
          />
          <Route 
            path="/invoices" 
            element={isAuthenticated ? <InvoicesPage /> : <Navigate to="/login" replace />} 
          />
          <Route 
            path="/orders" 
            element={isAuthenticated ? <OrdersTable /> : <Navigate to="/login" replace />} 
          />
          <Route path="*" element={<Navigate to={isAuthenticated ? "/products" : "/login"} replace />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
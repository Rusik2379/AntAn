import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Header from './components/Header';
import LoginPage from './components/LoginPage';
import RegisterPage from './components/RegisterPage';
import ProductTable from './components/ProductTable';
import OrdersTable from './components/OrdersTable';
import InvoicesPage from './components/InvoicesPage';
import UserInvoicePage from './components/UserInvoicePage';
import UserSchedulePage from './components/SchedulePage';
import SalaryTable from './components/SalaryTable';
import ScheduleTable from './components/ScheduleTable'; // Добавляем импорт ScheduleTable

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [userRole, setUserRole] = useState(null);
  const [authChecked, setAuthChecked] = useState(false);

  useEffect(() => {
    const checkAuth = () => {
      const token = localStorage.getItem('token');
      const role = localStorage.getItem('role');
      setIsAuthenticated(!!token);
      setUserRole(role);
      setAuthChecked(true);
    };
    
    checkAuth();
    
    window.addEventListener('storage', checkAuth);
    
    return () => {
      window.removeEventListener('storage', checkAuth);
    };
  }, []);

  const getDefaultRoute = () => {
    if (!isAuthenticated) return '/login';
    return userRole === 'ROLE_DIRECTOR' ? '/products' : '/userinvoice';
  };

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
            element={<Navigate to={getDefaultRoute()} replace />} 
          />
          <Route 
            path="/login" 
            element={isAuthenticated ? <Navigate to={getDefaultRoute()} replace /> : <LoginPage />} 
          />
          <Route 
            path="/register" 
            element={isAuthenticated ? <Navigate to={getDefaultRoute()} replace /> : <RegisterPage />} 
          />
          
          {/* Director-only routes */}
          {userRole === 'ROLE_DIRECTOR' && (
            <>
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
              <Route 
                path="/salary" 
                element={isAuthenticated ? <SalaryTable /> : <Navigate to="/login" replace />} 
              />
              <Route 
                path="/schedule" 
                element={isAuthenticated ? <ScheduleTable /> : <Navigate to="/login" replace />} 
              />
            </>
          )}
          
          {/* User-only routes */}
          <Route 
            path="/userinvoice" 
            element={isAuthenticated ? <UserInvoicePage /> : <Navigate to="/login" replace />} 
          />
          <Route 
            path="/userschedule" 
            element={isAuthenticated ? <UserSchedulePage /> : <Navigate to="/login" replace />} 
          />
          
          <Route path="*" element={<Navigate to={getDefaultRoute()} replace />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
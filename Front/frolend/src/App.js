import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Header from './components/Header';
import LoginPage from './components/LoginPage';
import RegisterPage from './components/RegisterPage';
import Product from './components/Product';
import InvoicesPage from './components/InvoicesPage';

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  useEffect(() => {
    setIsAuthenticated(!!localStorage.getItem('token'));
  }, []);

  return (
    <Router>
      <div className="App">
        {isAuthenticated && <Header />}
        <Routes>
          <Route path="/" element={isAuthenticated ? <Navigate to="/products" /> : <LoginPage />} />
          <Route path="/register" element={isAuthenticated ? <Navigate to="/products" /> : <RegisterPage />} />
          <Route path="/products" element={isAuthenticated ? <Product /> : <Navigate to="/" />} />
          <Route path="/invoices" element={isAuthenticated ? <InvoicesPage /> : <Navigate to="/" />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
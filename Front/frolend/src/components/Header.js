// src/components/Header.js
import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import './Header.css';

const Header = () => {
  const navigate = useNavigate();
  const role = localStorage.getItem('role');

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    window.dispatchEvent(new Event('storage'));
    navigate('/login');
  };

  if (role === 'ROLE_USER') {
    return (
      <div className="header-container">
        <header>
          <Link to="/userinvoice">
            <span className="header-item">Зарплата</span>
          </Link>
          <Link to="/userschedule">
            <span className="header-item">Расписание</span>
          </Link>
        </header>
        <button className="logout-btn" onClick={handleLogout}>
          Выйти
        </button>
      </div>
    );
  }

  return (
    <div className="header-container">
      <header>
        <Link to="/products">
          <span className="header-item">Товары</span>
        </Link>
        <Link to="/orders">
          <span className="header-item">Заказы</span>
        </Link>
        <Link to="/invoices">
          <span className="header-item">Накладные</span>
        </Link>
        <Link to="/salary">
          <span className="header-item">Зарплаты</span>
        </Link>
        <Link to="/schedule">
          <span className="header-item">Расписание</span>
        </Link>
      </header>
      <button className="logout-btn" onClick={handleLogout}>
        Выйти
      </button>
    </div>
  );
};

export default Header;
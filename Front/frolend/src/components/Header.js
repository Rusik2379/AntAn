import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import './Header.css';

const Header = () => {
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.removeItem('token');
    navigate('/');
  };

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
        <Link to="/salaries">
          <span className="header-item">Зарплаты</span>
        </Link>
        <Link to="/schedule">
          <span className="header-item">Расписание</span>
        </Link>
      </header>
      
      <button 
        className="logout-btn"
        onClick={handleLogout}
      >
        Выйти
      </button>
    </div>
  );
};

export default Header;
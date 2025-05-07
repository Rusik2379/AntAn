// src/components/LoginForm.js
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import './AuthForms.css';

const LoginForm = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
  
    try {
      const response = await axios.post('http://localhost:8080/login', {
        email,
        password
      }, {
        headers: {
          'Content-Type': 'application/json'
        }
      });
  
      if (!response.data.token) {
        throw new Error('Не получили токен от сервера');
      }
  
      localStorage.setItem('token', response.data.token);

      // Вызываем колбэк и делаем навигацию
      if (onLoginSuccess) onLoginSuccess();
      navigate('/products');

      // Принудительно обновляем страницу
      window.location.reload();
      
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Ошибка при входе');
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="auth-form">
      <h2>Авторизация</h2>
      
      {error && <div className="alert alert-danger">{error}</div>}
      
      <div className="form-group">
        <label>Логин</label>
        <input
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />
      </div>
      
      <div className="form-group">
        <label>Пароль</label>
        <input
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
      </div>
      
      <button type="submit" disabled={loading}>
        {loading ? 'Вход...' : 'Войти'}
      </button>
      
      <div style={{ marginTop: '15px', textAlign: 'center' }}>
        Нет аккаунта? <a href="/register" style={{ color: '#1890ff' }}>Регистрация</a>
      </div>
    </form>
  );
};

export default LoginForm;
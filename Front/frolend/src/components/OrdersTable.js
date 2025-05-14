import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './OrdersTable.css';

const OrdersTable = () => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchOrders = async () => {
      try {
        const token = localStorage.getItem('token');
        if (!token) {
          navigate('/login');
          return;
        }

        const oneYearAgo = new Date();
        oneYearAgo.setFullYear(oneYearAgo.getFullYear() - 1);
        const sinceDate = oneYearAgo.toISOString();

        const response = await fetch('http://localhost:8080/api/orders/merged', {
          method: 'POST', // Явно указываем метод
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({}), // Пустое тело, если не нужно передавать данные
        });


        if (!response.ok) {
          if (response.status === 401) {
            localStorage.removeItem('token');
            navigate('/login');
          }
          throw new Error(`Ошибка HTTP: ${response.status}`);
        }

        const data = await response.json();
        setOrders(data);
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchOrders();
  }, [navigate]);

  if (loading) {
    return <div className="loading">Загрузка заказов...</div>;
  }

  if (error) {
    return <div className="error">Ошибка: {error}</div>;
  }

  return (
    <div className="orders-container">
      <h1>Список заказов</h1>
      <div className="orders-table-wrapper">
        <table className="orders-table">
          <thead>
            <tr>
              <th>Изображение</th>
              <th>Наименование</th>
              <th>Цена</th>
              <th>Кол-во</th>
              <th>Источник</th>
              <th>Дата создания</th>
              <th>ID заказа</th>
            </tr>
          </thead>
          <tbody>
            {orders.map((order) => (
              <tr key={order.orderId}>
                <td>
                  <img 
                    src={order.imageUrl} 
                    alt={order.productName} 
                    className="order-image"
                    onError={(e) => {
                      e.target.onerror = null;
                      e.target.src = 'https://via.placeholder.com/100';
                    }}
                  />
                </td>
                <td>{order.productName}</td>
                <td>{order.price.toFixed(2)} ₽</td>
                <td>{order.quantity}</td>
                <td className="source-cell">
                  <span className={`source-badge ${order.source.toLowerCase()}`}>
                    {order.source}
                  </span>
                </td>
                <td>{new Date(order.createdAt).toLocaleString()}</td>
                <td>{order.orderId}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default OrdersTable;
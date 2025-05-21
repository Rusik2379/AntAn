// src/components/UserInvoicePage.js
import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './UserInvoicePage.css';

const UserInvoicePage = () => {
  const [salaries, setSalaries] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [packages, setPackages] = useState('');
  const [date, setDate] = useState('');

  const fetchSalaries = async () => {
    try {
      const token = localStorage.getItem('token')?.trim();
      if (!token) throw new Error('Токен не найден');

      const response = await axios.get('http://localhost:8080/api/salaries', {
        headers: {
          Authorization: `Bearer ${token}`
        }
      });
      setSalaries(response.data);
      setLoading(false);
    } catch (err) {
      setError(err.message || 'Ошибка загрузки зарплат');
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSalaries();
  }, []);

  const handleCloseShift = async (e) => {
    e.preventDefault();
    try {
      const token = localStorage.getItem('token')?.trim();
      if (!token) throw new Error('Токен не найден');
      if (!packages || !date) throw new Error('Введите количество посылок и дату');

      // Проверка формата даты (yyyy-MM-dd)
      const dateRegex = /^\d{4}-\d{2}-\d{2}$/;
      if (!dateRegex.test(date)) {
        throw new Error('Неверный формат даты. Используйте формат ГГГГ-ММ-ДД');
      }

      await axios.post(
        'http://localhost:8080/api/salaries/add',
        {
          packages: parseInt(packages),
          date: date
        },
        {
          headers: { Authorization: `Bearer ${token}` }
        }
      );
      setPackages('');
      setDate('');
      fetchSalaries();
    } catch (err) {
      setError(err.message || 'Ошибка при закрытии смены');
    }
  };

  const handleClaimAllSalaries = async () => {
    try {
      const token = localStorage.getItem('token')?.trim();
      if (!token) throw new Error('Токен не найден');

      await axios.post(
        'http://localhost:8080/api/salaries/claim-all',
        {},
        {
          headers: { Authorization: `Bearer ${token}` }
        }
      );
      fetchSalaries();
    } catch (err) {
      setError(err.message || 'Ошибка при получении зарплаты');
    }
  };

  // Форматирование даты в "01.01.2025"
  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('ru-RU', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    });
  };

  // Вычисление итогов
  const calculateTotals = () => {
    const totalPackages = salaries.reduce((sum, salary) => sum + (salary.rate ? Math.round(salary.sum / salary.rate) : 0), 0);
    const totalSalary = salaries.reduce((sum, salary) => sum + (salary.sum || 0), 0);
    return { totalPackages, totalSalary };
  };

  const { totalPackages, totalSalary } = calculateTotals();

  if (loading) {
    return <div className="loading">Загрузка данных о зарплате...</div>;
  }

  if (error) {
    return <div className="error">{error}</div>;
  }

  return (
    <div className="user-invoice-page">
      <h1>Моя зарплата</h1>

      {/* Форма для закрытия смены */}
      <form onSubmit={handleCloseShift} className="shift-form">
        <h3>Закрыть смену</h3>
        <div className="form-group">
          <label>Количество посылок</label>
          <input
            type="number"
            value={packages}
            onChange={(e) => setPackages(e.target.value)}
            placeholder="Введите количество посылок"
            min="0"
            required
          />
        </div>
        <div className="form-group">
          <label>Дата</label>
          <input
            type="date"
            value={date}
            onChange={(e) => setDate(e.target.value)}
            required
          />
        </div>
        <button type="submit">Закрыть смену</button>
      </form>

      {salaries.length === 0 ? (
        <p>Нет данных о зарплате</p>
      ) : (
        <div className="salary-list">
          <table className="salary-table">
            <thead>
              <tr>
                <th>Число</th>
                <th>Сумма посылок</th>
                <th>ЗП (руб.)</th>
              </tr>
            </thead>
            <tbody>
              {salaries.map(salary => (
                <tr key={salary.id}>
                  <td>{formatDate(salary.date)}</td>
                  <td>{salary.rate ? Math.round(salary.sum / salary.rate) : 'N/A'}</td>
                  <td>{salary.sum}</td>
                </tr>
              ))}
            </tbody>
            <tfoot>
              <tr className="total-row">
                <td>Итого</td>
                <td>{totalPackages}</td>
                <td>{totalSalary}</td>
              </tr>
            </tfoot>
          </table>
          <button onClick={handleClaimAllSalaries} className="claim-button">
            Забрать зарплату
          </button>
        </div>
      )}
    </div>
  );
};

export default UserInvoicePage;
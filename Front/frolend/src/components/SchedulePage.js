import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './SchedulePage.css';

const SchedulePage = () => {
  const [schedule, setSchedule] = useState({ workDays: [] });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [currentUser, setCurrentUser] = useState(null);
  const [weekStart, setWeekStart] = useState(() => {
    const today = new Date();
    const day = today.getDay();
    const diff = today.getDate() - day + (day === 0 ? -6 : 1);
    return new Date(today.setDate(diff));
  });

  const daysOfWeek = ['Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб', 'Вс'];

  const fetchData = async () => {
    try {
      setLoading(true);
      setError(null);
      
      const token = localStorage.getItem('token');
      if (!token) {
        throw new Error('Требуется авторизация');
      }

      const config = {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      };

      const userId = localStorage.getItem('userId');
      if (userId) {
        const userResponse = await axios.get(`http://localhost:8080/api/user/${userId}`, config);
        setCurrentUser(userResponse.data);
      }

      const scheduleResponse = await axios.get('http://localhost:8080/api/schedule', {
        ...config,
        params: { date: weekStart.toISOString().split('T')[0] }
      });
      setSchedule(scheduleResponse.data);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Ошибка загрузки данных');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [weekStart]);

  const handleAssign = async (dayIndex, slotIndex) => {
    try {
      setError(null);
      const token = localStorage.getItem('token');
      if (!token) {
        throw new Error('Требуется авторизация');
      }

      const date = new Date(weekStart);
      date.setDate(date.getDate() + dayIndex);
      const dateStr = date.toISOString().split('T')[0];

      await axios.post(
        'http://localhost:8080/api/schedule/assign',
        {},
        {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          },
          params: { date: dateStr }
        }
      );

      await fetchData();
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Ошибка записи на смену');
    }
  };

  const handleRemove = async (dayIndex, slotIndex) => {
    try {
      setError(null);
      const token = localStorage.getItem('token');
      if (!token) {
        throw new Error('Требуется авторизация');
      }

      const date = new Date(weekStart);
      date.setDate(date.getDate() + dayIndex);
      const dateStr = date.toISOString().split('T')[0];

      await axios.delete(
        'http://localhost:8080/api/schedule/remove',
        {
          headers: {
            'Authorization': `Bearer ${token}`
          },
          params: { date: dateStr }
        }
      );

      await fetchData();
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Ошибка отмены записи');
    }
  };

  const formatDate = (date) => {
    return new Date(date).toLocaleDateString('ru-RU', { 
      day: 'numeric', 
      month: 'numeric' 
    });
  };

  const getDayData = (dayIndex) => {
    const date = new Date(weekStart);
    date.setDate(date.getDate() + dayIndex);
    const dateStr = date.toISOString().split('T')[0];
    
    return schedule.workDays.find(day => 
      new Date(day.date).toISOString().split('T')[0] === dateStr
    ) || { workers: [], maxWorkers: 3 };
  };

  const isUserInSlot = (workers, slotIndex) => {
    if (!currentUser || !workers || workers.length <= slotIndex) return false;
    return workers[slotIndex]?.id === currentUser.id;
  };

  const changeWeek = (days) => {
    const newDate = new Date(weekStart);
    newDate.setDate(newDate.getDate() + days);
    setWeekStart(newDate);
  };

  if (!localStorage.getItem('token')) {
    return (
      <div className="auth-required">
        <h2>Для просмотра расписания требуется авторизация</h2>
        <button onClick={() => window.location.href = '/login'}>
          Войти в систему
        </button>
      </div>
    );
  }

  if (loading) {
    return <div className="loading">Загрузка расписания...</div>;
  }

  return (
    <div className="schedule-page">
      <h1>Расписание на неделю с {formatDate(weekStart)}</h1>
      
      <div className="week-navigation">
        <button onClick={() => changeWeek(-7)}>
          ← Предыдущая неделя
        </button>
        
        <button onClick={() => changeWeek(7)}>
          Следующая неделя →
        </button>
      </div>

      {error && <div className="error-message">{error}</div>}

      <table className="schedule-table">
        <thead>
          <tr>
            {daysOfWeek.map((day, index) => {
              const date = new Date(weekStart);
              date.setDate(date.getDate() + index);
              const dayData = getDayData(index);
              return (
                <th key={index} className={schedule.locked ? 'locked' : ''}>
                  <div>{day}</div>
                  <div className="date">{formatDate(date)}</div>
                  <div className="workers-count">
                    {dayData.workers?.length || 0}/{dayData.maxWorkers || 3}
                  </div>
                </th>
              );
            })}
          </tr>
        </thead>
        <tbody>
          {[0, 1, 2].map((slotIndex) => (
            <tr key={slotIndex}>
              {daysOfWeek.map((_, dayIndex) => {
                const dayData = getDayData(dayIndex);
                const worker = dayData.workers?.[slotIndex];
                const isCurrentUser = worker?.id === currentUser?.id;
                const isFull = dayData.workers?.length >= dayData.maxWorkers;
                
                return (
                  <td key={dayIndex} className={schedule.locked ? 'locked' : ''}>
                    {worker ? (
                      <div className="worker-slot">
                        <span className="worker-name">
                          {worker.firstName} {worker.lastName}
                        </span>
                        {isCurrentUser && !schedule.locked && (
                          <button
                            onClick={() => handleRemove(dayIndex, slotIndex)}
                            className="remove-button"
                            title="Отменить запись"
                          >
                            ×
                          </button>
                        )}
                      </div>
                    ) : (
                      !schedule.locked && !isFull && (
                        <button
                          onClick={() => handleAssign(dayIndex, slotIndex)}
                          className="assign-button"
                          title="Записаться на смену"
                        >
                          Записаться
                        </button>
                      )
                    )}
                  </td>
                );
              })}
            </tr>
          ))}
        </tbody>
      </table>

      {schedule.locked && (
        <div className="schedule-status">
          Расписание опубликовано и заблокировано для изменений
        </div>
      )}
    </div>
  );
};

export default SchedulePage;
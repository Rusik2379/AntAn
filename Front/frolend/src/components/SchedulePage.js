import React, { useState, useEffect, useCallback } from 'react';
import axios from 'axios';
import './SchedulePage.css';

const SchedulePage = () => {
  // Функция для получения даты текущего понедельника
  const getCurrentMonday = () => {
    const today = new Date();
    const day = today.getDay();
    const diff = today.getDate() - day + (day === 0 ? -6 : 2);
    const monday = new Date(today.setDate(diff));
    monday.setHours(0, 0, 0, 0);
    return monday;
  };

  // Состояния компонента
  const [currentMonday] = useState(getCurrentMonday());
  const [weekStart, setWeekStart] = useState(currentMonday.toISOString().split('T')[0]);
  const [schedule, setSchedule] = useState({ workDays: [] });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [currentUser, setCurrentUser] = useState(null);
  const role = localStorage.getItem('role');

  const daysOfWeek = [
    'Понедельник', 'Вторник', 'Среда', 
    'Четверг', 'Пятница', 'Суббота', 'Воскресенье'
  ];

  // Функция для получения заголовков авторизации
  const getAuthHeader = () => {
    const token = localStorage.getItem('token')?.trim();
    console.log('Token:', token); // Для отладки
    if (!token) {
        throw new Error('Токен не найден');
    }
    return { Authorization: `Bearer ${token}` };
};
  // Получение данных текущего пользователя
  const fetchCurrentUser = useCallback(async () => {
    try {
      const response = await axios.get('http://localhost:8080/api/schedule/my-schedule', {
        headers: getAuthHeader(),
        params: {
          startDate: weekStart,
          endDate: new Date(new Date(weekStart).setDate(new Date(weekStart).getDate() + 6)).toISOString().split('T')[0]
        }
      });
      if (response.data && response.data.length > 0) {
        setCurrentUser(response.data[0].workers.find(w => w.id === parseInt(localStorage.getItem('userId'))));
      }
    } catch (err) {
      console.error('Ошибка получения данных пользователя:', err);
      // Не блокируем загрузку если endpoint не доступен
    }
  }, [weekStart]);

  // Загрузка расписания на выбранную неделю
  const fetchSchedule = useCallback(async () => {
    try {
        setLoading(true);
        setError(null);
        const response = await axios.get('http://localhost:8080/api/schedule', {
            headers: getAuthHeader(),
            params: { date: weekStart }
        });
        setSchedule(response.data);
    } catch (err) {
        const errorMessage = err.message || 
                           (err.response?.status === 401 ? 'Требуется авторизация' : 'Ошибка загрузки расписания');
        setError(errorMessage);
    } finally {
        setLoading(false);
    }
}, [weekStart]);

useEffect(() => {
    const token = localStorage.getItem('token')?.trim();
    if (!token) {
        setError('Требуется авторизация');
        setLoading(false);
        return;
    }
    const loadData = async () => {
        try {
            await fetchCurrentUser();
            await fetchSchedule();
        } catch (err) {
            setError(err.message || 'Ошибка загрузки данных');
        }
    };
    loadData();
}, [fetchCurrentUser, fetchSchedule, weekStart]);

  // Переход на следующую неделю
  const handleNextWeek = () => {
    const nextWeekStart = new Date(weekStart);
    nextWeekStart.setDate(nextWeekStart.getDate() + 7);
    setWeekStart(nextWeekStart.toISOString().split('T')[0]);
  };

  // Переход на предыдущую неделю
  const handlePrevWeek = () => {
    const prevWeekStart = new Date(weekStart);
    prevWeekStart.setDate(prevWeekStart.getDate() - 7);
    
    // Не позволяем уходить раньше текущей недели
    if (prevWeekStart >= currentMonday) {
      setWeekStart(prevWeekStart.toISOString().split('T')[0]);
    }
  };

  // Запись на смену
  const handleAssignShift = async (dayIndex) => {
    try {
      const shiftDate = new Date(weekStart);
      shiftDate.setDate(shiftDate.getDate() + dayIndex);
      const shiftDateStr = shiftDate.toISOString().split('T')[0];

      await axios.post(
        'http://localhost:8080/api/schedule/assign',
        {},
        { 
          headers: getAuthHeader(),
          params: { date: shiftDateStr }
        }
      );
      await fetchSchedule();
    } catch (err) {
      setError(err.response?.data?.message || 'Ошибка записи на смену');
    }
  };

  // Отмена записи на смену
  const handleRemoveShift = async (dayIndex) => {
    try {
      const shiftDate = new Date(weekStart);
      shiftDate.setDate(shiftDate.getDate() + dayIndex);
      const shiftDateStr = shiftDate.toISOString().split('T')[0];

      await axios.delete(
        'http://localhost:8080/api/schedule/remove',
        { 
          headers: getAuthHeader(),
          params: { date: shiftDateStr }
        }
      );
      await fetchSchedule();
    } catch (err) {
      setError(err.response?.data?.message || 'Ошибка удаления из смены');
    }
  };

  // Очистка расписания на неделю (для директора)
  const handleClearSchedule = async () => {
    try {
      await axios.delete('http://localhost:8080/api/schedule/clear', {
        headers: getAuthHeader(),
        params: { date: weekStart }
      });
      await fetchSchedule();
    } catch (err) {
      setError(err.response?.data?.message || 'Ошибка очистки расписания');
    }
  };

  // Публикация расписания (для директора)
  const handlePublishSchedule = async () => {
    try {
      await axios.post(
        'http://localhost:8080/api/schedule/publish',
        {},
        { 
          headers: getAuthHeader(),
          params: { date: weekStart }
        }
      );
      await fetchSchedule();
      alert('Расписание опубликовано и заблокировано для изменений');
    } catch (err) {
      setError(err.response?.data?.message || 'Ошибка публикации расписания');
    }
  };

  // Получение смен для конкретного дня
  const getShiftsForDay = (dayIndex) => {
    const shiftDate = new Date(weekStart);
    shiftDate.setDate(shiftDate.getDate() + dayIndex);
    const shiftDateStr = shiftDate.toISOString().split('T')[0];
    
    return schedule.workDays?.find(day => day.date === shiftDateStr) || { workers: [] };
  };

  // Проверка доступности предыдущей недели
  const isPrevWeekAvailable = () => {
    const prevWeekStart = new Date(weekStart);
    prevWeekStart.setDate(prevWeekStart.getDate() - 7);
    return prevWeekStart >= currentMonday;
  };

  // Проверка доступности следующей недели
  const isNextWeekAvailable = () => {
    const nextWeekStart = new Date(weekStart);
    nextWeekStart.setDate(nextWeekStart.getDate() + 7);
    
    const maxAllowedDate = new Date(currentMonday);
    maxAllowedDate.setDate(maxAllowedDate.getDate() + 28); // Ограничение на 4 недели вперед
    
    return nextWeekStart <= maxAllowedDate;
  };

  // Проверка, записан ли текущий пользователь на смену
  const isUserAssigned = (workers) => {
    if (!currentUser || !workers || workers.length === 0) return false;
    return workers.some(worker => worker.id === currentUser.id);
  };

  // Форматирование даты
  const formatDate = (date) => {
    if (typeof date === 'string') {
      date = new Date(date);
    }
    return date.toLocaleDateString('ru-RU', {
      day: '2-digit',
      month: '2-digit'
    });
  };

  // Загрузка данных при монтировании и изменении недели
  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) {
      setError('Требуется авторизация');
      setLoading(false);
      return;
    }
    
    const loadData = async () => {
      await fetchCurrentUser();
      await fetchSchedule();
    };
    
    loadData();
  }, [fetchCurrentUser, fetchSchedule, weekStart]);

  // Если нет токена, показываем сообщение о необходимости авторизации
  if (!localStorage.getItem('token')) {
    return (
      <div className="schedule-page">
        <div className="auth-required">
          <h2>Для просмотра расписания требуется авторизация</h2>
          <button 
            className="login-button"
            onClick={() => window.location.href = '/login'}
          >
            Войти в систему
          </button>
        </div>
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
        <button 
          onClick={handlePrevWeek} 
          className="nav-button"
          disabled={!isPrevWeekAvailable()}
        >
          ← Предыдущая неделя
        </button>
        
        <button 
          onClick={handleNextWeek} 
          className="nav-button"
          disabled={!isNextWeekAvailable()}
        >
          Следующая неделя →
        </button>
      </div>
      
      {role === 'ROLE_DIRECTOR' && (
        <div className="admin-controls">
          <button 
            onClick={handleClearSchedule} 
            className="clear-schedule-button"
            disabled={loading || schedule.locked}
          >
            Очистить расписание
          </button>
          
          <button 
            onClick={handlePublishSchedule} 
            className="publish-button"
            disabled={loading || schedule.locked}
          >
            Опубликовать расписание
          </button>
        </div>
      )}
      
      {error && <div className="error-message">{error}</div>}

      <div className="schedule-container">
        <table className="schedule-table">
          <thead>
            <tr>
              {daysOfWeek.map((day, index) => {
                const date = new Date(weekStart);
                date.setDate(date.getDate() + index);
                const dayData = getShiftsForDay(index);
                const isDayLocked = schedule.locked;
                
                return (
                  <th key={index} className={isDayLocked ? 'locked' : ''}>
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
            {[0, 1, 2].map(slot => (
              <tr key={slot}>
                {daysOfWeek.map((_, dayIndex) => {
                  const dayData = getShiftsForDay(dayIndex);
                  const worker = dayData.workers?.[slot];
                  const isSlotFilled = !!worker;
                  const isFull = (dayData.workers?.length || 0) >= (dayData.maxWorkers || 3);
                  const isLocked = schedule.locked;
                  const isAssigned = isUserAssigned(dayData.workers);
                  
                  return (
                    <td 
                      key={dayIndex} 
                      className={`${isFull ? 'full' : ''} ${isLocked ? 'locked' : ''}`}
                    >
                      {isSlotFilled ? (
                        <div className="shift-slot">
                            <span className="user-name">
                                {worker.firstName} {worker.lastName}
                            </span>
                            {currentUser && 
                                (currentUser.id === worker.id || role === 'ROLE_DIRECTOR') && 
                                !isLocked && (
                                    <button
                                        onClick={() => handleRemoveShift(dayIndex)}
                                        className="remove-button"
                                        title="Отменить запись"
                                    >
                                        ×
                                    </button>
                                )}
                        </div>
                      ) : (
                        !isLocked && !isAssigned && (
                            <button
                                onClick={() => handleAssignShift(dayIndex)}
                                className="assign-button"
                                disabled={isFull}
                                title={isFull ? 'День уже заполнен' : 'Записаться на смену'}
                            >
                                {isFull ? 'Заполнено' : 'Записаться'}
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
      </div>

      {schedule.locked && (
        <div className="schedule-status">
          Расписание опубликовано и заблокировано для изменений
        </div>
      )}
    </div>
  );
};

export default SchedulePage;
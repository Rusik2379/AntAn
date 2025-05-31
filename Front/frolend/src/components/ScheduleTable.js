import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './ScheduleTable.css';

const ScheduleTable = () => {
    const [schedule, setSchedule] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const fetchSchedule = async () => {
        try {
            setLoading(true);
            setError(null);
            
            const token = localStorage.getItem('token');
            if (!token || token === 'undefined') {
                throw new Error('Требуется авторизация. Пожалуйста, войдите в систему.');
            }

            const response = await axios.get('http://localhost:8080/api/schedule/all-schedule', {
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });

            const formattedSchedule = formatScheduleData(response.data);
            setSchedule(formattedSchedule);
            
        } catch (err) {
            let errorMessage = 'Ошибка при загрузке расписания';
            
            if (err.response) {
                if (err.response.status === 401) {
                    errorMessage = 'Ошибка авторизации. Пожалуйста, войдите снова.';
                } else if (err.response.status === 404) {
                    errorMessage = 'Расписание не найдено';
                } else if (err.response.data && err.response.data.message) {
                    errorMessage = err.response.data.message;
                }
            } else if (err.message) {
                errorMessage = err.message;
            }

            setError(errorMessage);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchSchedule();
    }, []);

    const formatScheduleData = (workDays) => {
        if (!Array.isArray(workDays)) return [];
        
        const result = [];
        
        workDays.forEach(day => {
            if (!day || !day.workers) return;

            day.workers.forEach(worker => {
                result.push({
                    employee: `${worker.firstName || ''} ${worker.lastName || ''}`.trim() || 'Без имени',
                    dayOfWeek: getDayOfWeek(day.date),
                    date: day.date
                });
            });
        });
        
        return result;
    };

    const getDayOfWeek = (dateString) => {
        const days = ['Воскресенье', 'Понедельник', 'Вторник', 'Среда', 'Четверг', 'Пятница', 'Суббота'];
        const date = new Date(dateString);
        return days[date.getDay()];
    };

    const formatDate = (dateString) => {
        const date = new Date(dateString);
        return date.toLocaleDateString('ru-RU');
    };

    const handleRefresh = () => {
        fetchSchedule();
    };

    if (loading) {
        return <div>Загрузка расписания...</div>;
    }

    if (error) {
        return (
            <div>
                <div>{error}</div>
                <button onClick={handleRefresh}>Обновить</button>
            </div>
        );
    }

    return (
        <div className="schedule-container">
            <div className="schedule-header">
                <h2>Расписание сотрудников</h2>
                <button onClick={handleRefresh} disabled={loading}>
                    Обновить
                </button>
            </div>

            <table className="schedule-table">
                <thead>
                    <tr>
                        <th>Сотрудник</th>
                        <th>День недели</th>
                        <th>Дата</th>
                    </tr>
                </thead>
                <tbody>
                    {schedule.length > 0 ? (
                        schedule.map((item, index) => (
                            <tr key={index}>
                                <td>{item.employee}</td>
                                <td>{item.dayOfWeek}</td>
                                <td>{formatDate(item.date)}</td>
                            </tr>
                        ))
                    ) : (
                        <tr>
                            <td colSpan="3">Нет данных о расписании</td>
                        </tr>
                    )}
                </tbody>
            </table>
        </div>
    );
};

export default ScheduleTable;
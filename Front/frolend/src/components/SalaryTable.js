import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './SalaryTable.css';

const SalaryTable = () => {
    const [salaries, setSalaries] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [isDirectorOrAdmin, setIsDirectorOrAdmin] = useState(false);

    useEffect(() => {
        const fetchData = async () => {
            const token = localStorage.getItem('token');
            const role = localStorage.getItem('role');

            if (!token) {
                setError('Требуется авторизация');
                setLoading(false);
                return;
            }

            const isElevated = role === 'ROLE_ADMIN' || role === 'ROLE_DIRECTOR';
            setIsDirectorOrAdmin(isElevated);

            try {
                const endpoint = isElevated ? '/api/salaries/all' : '/api/salaries';
                const response = await axios.get(endpoint, {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json'
                    }
                });
                
                // Сортируем по дате (от новых к старым)
                const sortedSalaries = response.data.sort((a, b) => {
                    return new Date(b.date) - new Date(a.date);
                });
                
                setSalaries(sortedSalaries);
            } catch (err) {
                setError(err.response?.data?.message || err.message || 'Ошибка при загрузке данных');
                console.error('Error fetching salaries:', err);
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, []);

    const handleMarkAsPaid = async (id) => {
        try {
            const token = localStorage.getItem('token');
            await axios.patch(`/api/salaries/${id}/pay`, {}, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });

            setSalaries(prev =>
                prev.map(salary =>
                    salary.id === id ? { ...salary, paid: 'Yes' } : salary
                )
            );
        } catch (err) {
            alert(err.response?.data?.message || 'Ошибка при обновлении статуса');
        }
    };

    const handleClaimAll = async () => {
        if (window.confirm('Вы уверены, что хотите подтвердить все выплаты?')) {
            try {
                const token = localStorage.getItem('token');
                await axios.post('/api/salaries/claim-all', {}, {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json'
                    }
                });

                setSalaries(prev =>
                    prev.map(salary =>
                        salary.paid === 'No' ? { ...salary, paid: 'Yes' } : salary
                    )
                );
            } catch (err) {
                alert(err.response?.data?.message || 'Ошибка при массовом подтверждении');
            }
        }
    };

    const formatDate = (dateString) => {
        if (!dateString) return 'Не указана';
        const date = new Date(dateString);
        return date.toLocaleDateString('ru-RU', {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    };

    if (loading) {
        return (
            <div className="salary-container">
                <div className="loading-spinner">Загрузка данных о зарплатах...</div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="salary-container">
                <div className="error-message">{error}</div>
            </div>
        );
    }

    return (
        <div className="salary-container">
            <h2>Управление зарплатами</h2>

            {!isDirectorOrAdmin && salaries.some(s => s.paid === 'No') && (
                <button
                    className="claim-all-btn"
                    onClick={handleClaimAll}
                >
                    Подтвердить все выплаты
                </button>
            )}

            <div className="table-responsive">
                <table className="salary-table">
                    <thead>
                        <tr>
                            <th>Сотрудник</th>
                            <th>Сумма</th>
                            <th>Ставка</th>
                            <th>Дата</th>
                            <th>Статус</th>
                            {!isDirectorOrAdmin && <th>Действия</th>}
                        </tr>
                    </thead>
                    <tbody>
                        {salaries.length > 0 ? (
                            salaries.map(salary => (
                                <tr
                                    key={salary.id}
                                    className={salary.paid === 'Yes' ? 'paid-row' : 'unpaid-row'}
                                >
                                    <td>
                                        {salary.user?.firstName || 'Неизвестный'} {salary.user?.lastName || 'сотрудник'}
                                    </td>
                                    <td>{salary.sum?.toFixed(2) || '0.00'} ₽</td>
                                    <td>{salary.rate?.toFixed(2) || '0.00'} ₽/ед.</td>
                                    <td>{formatDate(salary.date)}</td>
                                    <td>
                                        <span className={`status-badge ${salary.paid === 'Yes' ? 'paid' : 'unpaid'}`}>
                                            {salary.paid === 'Yes' ? 'Выплачено' : 'Ожидает выплаты'}
                                        </span>
                                    </td>
                                    {!isDirectorOrAdmin && salary.paid === 'No' && (
                                        <td>
                                            <button
                                                className="pay-btn"
                                                onClick={() => handleMarkAsPaid(salary.id)}
                                            >
                                                Подтвердить выплату
                                            </button>
                                        </td>
                                    )}
                                </tr>
                            ))
                        ) : (
                            <tr>
                                <td colSpan={isDirectorOrAdmin ? 5 : 6} className="no-data">
                                    Нет данных о зарплатах
                                </td>
                            </tr>
                        )}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default SalaryTable;
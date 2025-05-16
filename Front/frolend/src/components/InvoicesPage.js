import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import './InvoicesPage.css';

const InvoicesPage = () => {
    const [invoices, setInvoices] = useState([]);
    const [file, setFile] = useState(null);
    const [fileName, setFileName] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const navigate = useNavigate();

    // Проверка валидности токена
    const validateToken = () => {
        const token = localStorage.getItem('token');
        if (!token) {
            navigate('/login');
            return false;
        }
        return true;
    };

    // Загрузка списка накладных
    const loadInvoices = async () => {
        if (!validateToken()) return;

        try {
            setLoading(true);
            setError('');
            const token = localStorage.getItem('token');
            const response = await axios.get('http://localhost:8080/api/invoices', {
                headers: { 
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });
            setInvoices(response.data);
        } catch (err) {
            handleApiError(err);
        } finally {
            setLoading(false);
        }
    };

    // Обработка ошибок API
    const handleApiError = (error) => {
      console.error('API Error:', error);
      
      if (error.response?.status === 401) {
          // Токен недействителен или просрочен
          handleTokenExpired();
      } else {
          const errorMessage = error.response?.data?.message || 
                             (typeof error.response?.data === 'string' ? error.response.data : 'Произошла ошибка');
          setError(errorMessage);
      }
  };
  
  const handleTokenExpired = () => {
      localStorage.removeItem('token');
      navigate('/login');
      alert('Ваша сессия истекла. Пожалуйста, войдите снова.');
  };

    // Обработка изменения файла
    const handleFileChange = (e) => {
        const selectedFile = e.target.files[0];
        if (!selectedFile) return;

        if (selectedFile.type !== 'application/pdf') {
            setError('Только PDF файлы разрешены');
            return;
        }

        setFile(selectedFile);
        if (!fileName) {
            setFileName(selectedFile.name.replace('.pdf', ''));
        }
        setError('');
    };

    // Загрузка файла на сервер
    const handleUpload = async (e) => {
        e.preventDefault();
        if (!validateToken()) return;
        
        if (!file) {
            setError('Выберите файл');
            return;
        }

        const formData = new FormData();
        formData.append('file', file);
        formData.append('fileName', fileName);

        try {
            setLoading(true);
            setError('');
            setSuccess('');
            
            const token = localStorage.getItem('token');
            await axios.post('http://localhost:8080/api/invoices/upload', formData, {
                headers: {
                    'Content-Type': 'multipart/form-data',
                    'Authorization': `Bearer ${token}`
                },
                withCredentials: true
            });
            
            setSuccess('Накладная успешно загружена');
            await loadInvoices();
            setFile(null);
            setFileName('');
        } catch (err) {
            handleApiError(err);
        } finally {
            setLoading(false);
        }
    };

    // Скачивание накладной
    const downloadInvoice = async (id) => {
        if (!validateToken()) return;

        try {
            const token = localStorage.getItem('token');
            const response = await axios.get(`http://localhost:8080/api/invoices/${id}`, {
                headers: { 
                    'Authorization': `Bearer ${token}` 
                },
                responseType: 'blob'
            });
            
            const url = window.URL.createObjectURL(new Blob([response.data]));
            const link = document.createElement('a');
            link.href = url;
            
            // Получаем имя файла из заголовков
            const contentDisposition = response.headers['content-disposition'];
            let filename = 'Накладная.pdf';
            if (contentDisposition) {
                const match = contentDisposition.match(/filename="?(.+)"?/);
                if (match?.[1]) filename = match[1];
            }
            
            link.setAttribute('download', filename);
            document.body.appendChild(link);
            link.click();
            link.parentNode.removeChild(link);
            window.URL.revokeObjectURL(url);
        } catch (err) {
            handleApiError(err);
        }
    };

    // Загрузка данных при монтировании компонента
    useEffect(() => {
        if (validateToken()) {
            loadInvoices();
        }
    }, [navigate]);

    return (
        <div className="invoices-container">
            <h2>Накладные</h2>
            
            <div className="upload-section">
                <h3>Загрузить новую накладную</h3>
                <form onSubmit={handleUpload}>
                    <div className="form-group">
                        <label>PDF файл:</label>
                        <input 
                            type="file" 
                            accept="application/pdf"
                            onChange={handleFileChange}
                            required
                            disabled={loading}
                        />
                    </div>
                    
                    <div className="form-group">
                        <label>Название файла:</label>
                        <input
                            type="text"
                            value={fileName}
                            onChange={(e) => setFileName(e.target.value)}
                            required
                            disabled={loading}
                        />
                    </div>
                    
                    <button type="submit" disabled={loading}>
                        {loading ? 'Загрузка...' : 'Загрузить'}
                    </button>
                    
                    {error && <div className="error-message">{error}</div>}
                    {success && <div className="success-message">{success}</div>}
                </form>
            </div>
            
            <div className="invoices-list">
                <h3>Список накладных</h3>
                {loading && invoices.length === 0 ? (
                    <div className="loading-message">Загрузка...</div>
                ) : invoices.length === 0 ? (
                    <div className="empty-message">Нет доступных накладных</div>
                ) : (
                    <table>
                        <thead>
                            <tr>
                                <th>Название</th>
                                <th>Дата</th>
                                <th>Действия</th>
                            </tr>
                        </thead>
                        <tbody>
                        {invoices.map(invoice => {
                            console.log('Invoice data:', invoice); // Для отладки
                            return (
                                <tr key={invoice.id}>
                                    <td>{invoice.fileName || 'Без названия'}</td>
                                    <td>{invoice.date ? new Date(invoice.date).toLocaleString() : 'Дата не указана'}</td>
                                    <td>
                                        <button 
                                            onClick={() => downloadInvoice(invoice.id)}
                                            disabled={loading}
                                        >
                                            Скачать
                                        </button>
                                    </td>
                                </tr>
                            );
                        })}
                        </tbody>
                    </table>
                )}
            </div>
        </div>
    );
};

export default InvoicesPage;
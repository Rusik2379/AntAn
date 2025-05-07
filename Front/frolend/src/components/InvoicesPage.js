import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import './InvoicesPage.css';

const API_BASE_URL = 'http://localhost:8080'; 

const InvoicesPage = () => {
  const [invoices, setInvoices] = useState([]);
  const [selectedFile, setSelectedFile] = useState(null);
  const [fileName, setFileName] = useState('');
  const [companyId, setCompanyId] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) {
      navigate('/');
      return;
    }

    fetchInvoices();
  }, [navigate]);

  const fetchInvoices = async () => {
    try {
      setIsLoading(true);
      const response = await axios.get('/api/invoices', {
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}`
        }
      });
      setInvoices(response.data);
    } catch (err) {
      setError('Ошибка при загрузке накладных');
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  const handleFileChange = (e) => {
    setSelectedFile(e.target.files[0]);
  };

  const handleUpload = async (e) => {
    e.preventDefault();
    
    if (!selectedFile || !fileName || !companyId) {
      setError('Все поля обязательны для заполнения');
      return;
    }

    const formData = new FormData();
    formData.append('file', selectedFile);
    formData.append('fileName', fileName);
    formData.append('companyId', companyId);

    try {
      setIsLoading(true);
      await axios.post(`${API_BASE_URL}/api/invoices/upload`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
          Authorization: `Bearer ${localStorage.getItem('token')}`
        }
      });
      
      // Обновляем список после загрузки
      await fetchInvoices();
      
      // Сбрасываем форму
      setSelectedFile(null);
      setFileName('');
      setCompanyId('');
      setError('');
    } catch (err) {
      setError('Ошибка при загрузке файла');
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  const downloadInvoice = async (id) => {
    try {
      const response = await axios.get(`/api/invoices/${id}`, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}`
        },
        responseType: 'blob'
      });
      
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      
      // Получаем имя файла из заголовков ответа
      const contentDisposition = response.headers['content-disposition'];
      let filename = 'invoice.pdf';
      if (contentDisposition) {
        const filenameMatch = contentDisposition.match(/filename="?(.+)"?/);
        if (filenameMatch && filenameMatch[1]) {
          filename = filenameMatch[1];
        }
      }
      
      link.setAttribute('download', filename);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (err) {
      setError('Ошибка при скачивании файла');
      console.error(err);
    }
  };

  return (
    <div className="invoices-container">
      <h2>Накладные</h2>
      
      <div className="upload-section">
        <h3>Загрузить новую накладную</h3>
        <form onSubmit={handleUpload}>
          <div className="form-group">
            <label>Файл PDF:</label>
            <input 
              type="file" 
              accept="application/pdf" 
              onChange={handleFileChange} 
              required 
            />
          </div>
          
          <div className="form-group">
            <label>Название файла:</label>
            <input
              type="text"
              value={fileName}
              onChange={(e) => setFileName(e.target.value)}
              required
            />
          </div>
          
          <div className="form-group">
            <label>ID компании:</label>
            <input
              type="number"
              value={companyId}
              onChange={(e) => setCompanyId(e.target.value)}
              required
            />
          </div>
          
          <button type="submit" disabled={isLoading}>
            {isLoading ? 'Загрузка...' : 'Загрузить'}
          </button>
          
          {error && <p className="error-message">{error}</p>}
        </form>
      </div>
      
      <div className="invoices-list">
        <h3>Список накладных</h3>
        {isLoading ? (
          <p>Загрузка...</p>
        ) : invoices.length === 0 ? (
          <p>Накладные отсутствуют</p>
        ) : (
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Название</th>
                <th>Дата</th>
                <th>Компания</th>
                <th>Действия</th>
              </tr>
            </thead>
            <tbody>
              {invoices.map((invoice) => (
                <tr key={invoice.id}>
                  <td>{invoice.id}</td>
                  <td>{invoice.fileName}</td>
                  <td>{new Date(invoice.date).toLocaleDateString()}</td>
                  <td>{invoice.company?.name || 'Неизвестно'}</td>
                  <td>
                    <button onClick={() => downloadInvoice(invoice.id)}>
                      Скачать
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
};

export default InvoicesPage;
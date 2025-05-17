import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './OrdersTable.css';

const OrdersTable = () => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [imageCache, setImageCache] = useState({});
  const navigate = useNavigate();

  // SVG fallback изображение
  const getFallbackImage = () => {
    return 'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="100" height="100" viewBox="0 0 100 100"><rect width="100" height="100" fill="%23eee"/><text x="50%" y="50%" dominant-baseline="middle" text-anchor="middle" fill="%23aaa">No Image</text></svg>';
  };

  useEffect(() => {
    const fetchOrders = async () => {
      try {
        const token = localStorage.getItem('token');
        if (!token) {
          navigate('/login');
          return;
        }

        const response = await fetch('http://localhost:8080/api/orders/merged', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          },
          body: JSON.stringify({}),
        });

        if (!response.ok) {
          if (response.status === 401) {
            localStorage.removeItem('token');
            navigate('/login');
          }
          throw new Error(`HTTP error: ${response.status}`);
        }

        const data = await response.json();
        setOrders(data);
        loadOzonImages(data);
      } catch (err) {
        console.error('Error fetching orders:', err);
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchOrders();
  }, [navigate]);

  const loadOzonImages = async (orders) => {
    const ozonOrders = orders.filter(order => order.source === 'ozon' && order.sku);
    const newImageCache = {...imageCache};
    
    for (const order of ozonOrders) {
      try {
        if (!newImageCache[order.sku] && !order.imageUrl) {
          console.log(`Fetching image for SKU: ${order.sku}`);
          const response = await fetch(`http://localhost:8080/api/ozon/products/${order.sku}/pictures`);
          
          if (!response.ok) {
            console.error(`Failed to fetch image for SKU ${order.sku}:`, response.status);
            continue;
          }
  
          const data = await response.json();
          console.log('Image API response:', data);
  
          if (data.items && data.items.length > 0 && data.items[0].url) {
            newImageCache[order.sku] = data.items[0].url;
            setOrders(prevOrders => 
              prevOrders.map(o => 
                o.sku === order.sku ? {...o, imageUrl: data.items[0].url} : o
              )
            );
          }
        }
      } catch (err) {
        console.error(`Error loading image for SKU ${order.sku}:`, err);
      }
    }
    
    setImageCache(newImageCache);
  };

  if (loading) return <div className="loading">Загрузка заказов...</div>;
  if (error) return <div className="error">Ошибка: {error}</div>;

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
              <tr key={`${order.orderId}-${order.sku || order.offerId}`}>
                <td>
                  <img 
                    src={order.imageUrl || imageCache[order.sku] || getFallbackImage()}
                    alt={order.productName} 
                    className="order-image"
                    onError={(e) => {
                      e.target.onerror = null;
                      e.target.src = getFallbackImage();
                    }}
                    loading="lazy"
                  />
                </td>
                <td>{order.productName}</td>
                <td>{order.price?.toFixed(2)} ₽</td>
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
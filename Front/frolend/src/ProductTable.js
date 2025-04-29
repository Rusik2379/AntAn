import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './ProductTable.css';

const ProductTable = () => {
    const [products, setProducts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [ozonImages, setOzonImages] = useState({});
    const [imageLoading, setImageLoading] = useState({});

    useEffect(() => {
        const fetchData = async () => {
            try {
                // 1. Загружаем список товаров
                const response = await axios.get('http://localhost:8080/api/products/merged');
                setProducts(response.data);

                // 2. Инициализируем состояние загрузки изображений
                const initialImageLoading = {};
                const initialOzonImages = {};
                
                response.data.forEach(product => {
                    if (product.ozonData) {
                        initialImageLoading[product.ozonData.product_id] = true;
                        initialOzonImages[product.ozonData.product_id] = null;
                    }
                });
                
                setImageLoading(initialImageLoading);
                setOzonImages(initialOzonImages);
                setLoading(false);

                // 3. Загружаем изображения для каждого товара Ozon
                const ozonProducts = response.data.filter(p => p.ozonData);
                
                ozonProducts.forEach(async (product) => {
                    const productId = product.ozonData.product_id;
                    try {
                        const res = await axios.get(
                            `http://localhost:8080/api/ozon/products/${productId}/pictures`
                        );
                        
                        // Проверяем разные варианты расположения фото
                        const item = res.data?.items?.[0];
                        const imageUrl = item?.primary_photo?.[0] || 
                                        item?.photo?.[0] || 
                                        (item?.photo_360?.length ? item.photo_360[0] : null);

                        if (imageUrl) {
                            setOzonImages(prev => ({
                                ...prev,
                                [productId]: imageUrl
                            }));
                        } else {
                            console.warn(`Нет доступных изображений для товара ${productId}`);
                        }
                    } catch (err) {
                        console.error(`Ошибка загрузки изображения для товара ${productId}:`, err);
                    } finally {
                        setImageLoading(prev => ({
                            ...prev,
                            [productId]: false
                        }));
                    }
                });

            } catch (err) {
                setError('Ошибка при загрузке данных: ' + err.message);
                setLoading(false);
            }
        };

        fetchData();
    }, []);

    if (loading) return (
        <div className="loading-container">
            <div className="loading-spinner"></div>
            <p className="loading-text">Загрузка данных...</p>
        </div>
    );
    
    if (error) return <div>{error}</div>;


    return (
        <div style={{ overflowX: 'auto' }}>
            <h2>Товары с Ozon и Wildberries</h2>
            <table style={{ borderCollapse: 'collapse', width: '100%' }}>
                <thead>
                    <tr style={{ backgroundColor: '#f2f2f2' }}>
                        <th style={{ border: '1px solid #ddd', padding: '8px' }}>Изображение</th>
                        <th style={{ border: '1px solid #ddd', padding: '8px' }}>Артикул</th>
                        <th style={{ border: '1px solid #ddd', padding: '8px' }}>Название</th>
                        <th style={{ border: '1px solid #ddd', padding: '8px' }}>На Ozon</th>
                        <th style={{ border: '1px solid #ddd', padding: '8px' }}>На Wildberries</th>
                        <th style={{ border: '1px solid #ddd', padding: '8px' }}>SKU WB</th>
                        <th style={{ border: '1px solid #ddd', padding: '8px' }}>ID Ozon</th>
                        <th style={{ border: '1px solid #ddd', padding: '8px' }}>ID WB</th>
                    </tr>
                </thead>
                <tbody>
                    {products.map((product, index) => {
                        const name = product.wbData?.title || product.ozonData?.name || '-';
                        const productId = product.ozonData?.product_id;
                        const ozonImageUrl = productId ? ozonImages[productId] : null;
                        const isLoading = productId ? imageLoading[productId] : false;

                        return (
                            <tr key={index}>
                               <td style={{ border: '1px solid #ddd', padding: '8px', textAlign: 'center' }}>
                                    {product.ozonData ? (
                                        isLoading ? (
                                            <div style={{ 
                                                width: '80px', 
                                                height: '80px', 
                                                display: 'flex',
                                                alignItems: 'center',
                                                justifyContent: 'center'
                                            }}>
                                                <div className="table-spinner"></div>
                                            </div>
                                        ) : ozonImageUrl ? (
                                            <img 
                                                src={ozonImageUrl} 
                                                alt={name}
                                                style={{ 
                                                    maxWidth: '80px', 
                                                    maxHeight: '80px',
                                                    objectFit: 'contain'
                                                }}
                                                onError={(e) => {
                                                    e.target.onerror = null;
                                                    e.target.src = 'https://via.placeholder.com/80?text=No+Image';
                                                }}
                                            />
                                        ) : (
                                            <div style={{ 
                                                width: '80px', 
                                                height: '80px', 
                                                lineHeight: '80px',
                                                textAlign: 'center',
                                                color: '#999'
                                            }}>
                                                Нет фото
                                            </div>
                                        )
                                    ) : (
                                        <div style={{ 
                                            width: '80px', 
                                            height: '80px', 
                                            lineHeight: '80px',
                                            textAlign: 'center',
                                            color: '#999'
                                        }}>
                                            -
                                        </div>
                                    )}
                                </td>
                                <td style={{ border: '1px solid #ddd', padding: '8px' }}>{product.vendorCode}</td>
                                <td style={{ border: '1px solid #ddd', padding: '8px' }}>{name}</td>
                                <td style={{ border: '1px solid #ddd', padding: '8px' }}>
                                    {product.ozonData ? 'Есть' : 'Нет'}
                                </td>
                                <td style={{ border: '1px solid #ddd', padding: '8px' }}>
                                    {product.wbData ? 'Есть' : 'Нет'}
                                </td>
                                <td style={{ border: '1px solid #ddd', padding: '8px' }}>
                                    {product.wbData?.skus?.join(', ') || '-'}
                                </td>
                                <td style={{ border: '1px solid #ddd', padding: '8px' }}>
                                    {product.ozonData?.product_id || '-'}
                                </td>
                                <td style={{ border: '1px solid #ddd', padding: '8px' }}>
                                    {product.wbData?.nmID || '-'}
                                </td>
                            </tr>
                        );
                    })}
                </tbody>
            </table>
        </div>
    );
};

export default ProductTable;
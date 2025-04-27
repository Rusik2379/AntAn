import React, { useState, useEffect } from 'react';
import axios from 'axios';

const ProductTable = () => {
    const [products, setProducts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchProducts = async () => {
            try {
                // Запросы к обоим API
                const [ozonResponse, wbResponse] = await Promise.all([
                    axios.get('http://localhost:8080/api/ozon/products'),
                    axios.get('http://localhost:8080/api/wb/wbproducts')
                ]);

                // Обрабатываем данные
                const ozonProducts = ozonResponse.data.result.items;
                const wbProducts = wbResponse.data;

                // Создаем объект для группировки по артикулу
                const productsMap = {};

                // Добавляем товары Ozon
                ozonProducts.forEach(product => {
                    if (!productsMap[product.offer_id]) {
                        productsMap[product.offer_id] = {
                            vendorCode: product.offer_id,
                            ozonData: product,
                            wbData: null
                        };
                    } else {
                        productsMap[product.offer_id].ozonData = product;
                    }
                });

                // Добавляем товары Wildberries
                wbProducts.forEach(product => {
                    if (!productsMap[product.vendorCode]) {
                        productsMap[product.vendorCode] = {
                            vendorCode: product.vendorCode,
                            ozonData: null,
                            wbData: product
                        };
                    } else {
                        productsMap[product.vendorCode].wbData = product;
                    }
                });

                // Преобразуем объект в массив
                const mergedProducts = Object.values(productsMap);

                setProducts(mergedProducts);
                setLoading(false);
            } catch (err) {
                setError('Ошибка при загрузке данных: ' + err.message);
                setLoading(false);
            }
        };

        fetchProducts();
    }, []);

    if (loading) return <div>Загрузка...</div>;
    if (error) return <div>{error}</div>;

    return (
        <div>
            <h2>Товары с Ozon и Wildberries</h2>
            <table style={{ borderCollapse: 'collapse', width: '100%' }}>
                <thead>
                    <tr style={{ backgroundColor: '#f2f2f2' }}>
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
                    {products.map((product, index) => (
                        <tr key={index}>
                            <td style={{ border: '1px solid #ddd', padding: '8px' }}>{product.vendorCode}</td>
                            <td style={{ border: '1px solid #ddd', padding: '8px' }}>
                                {product.wbData?.title || product.ozonData?.name || '-'}
                            </td>
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
                    ))}
                </tbody>
            </table>
        </div>
    );
};

export default ProductTable;
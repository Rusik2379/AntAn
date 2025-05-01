import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './ProductTable.css';

const ProductTable = () => {
    const [products, setProducts] = useState([]);
    const [filteredProducts, setFilteredProducts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [ozonImages, setOzonImages] = useState({});
    const [wbImages, setWbImages] = useState({});
    const [imageLoading, setImageLoading] = useState({});
    const [searchTerm, setSearchTerm] = useState('');
    const [platformFilter, setPlatformFilter] = useState('all');

    useEffect(() => {
        const fetchData = async () => {
            try {
                const response = await axios.get('http://localhost:8080/api/products/merged');
                setProducts(response.data);
                applyFilters(response.data, searchTerm, platformFilter);

                const initialImageLoading = {};
                const initialOzonImages = {};
                const initialWbImages = {};
                
                response.data.forEach(product => {
                    // Инициализация для Ozon
                    if (product.ozonData) {
                        const productId = product.ozonData.product_id;
                        initialImageLoading[`ozon_${productId}`] = true;
                        initialOzonImages[productId] = null;
                    }
                    
                    // Инициализация для WB
                    if (product.wbData) {
                        initialImageLoading[`wb_${product.wbData.nmID}`] = false;
                        if (product.wbData.imageUrl) {
                            initialWbImages[product.wbData.nmID] = product.wbData.imageUrl;
                        }
                    }
                });
                
                setImageLoading(initialImageLoading);
                setOzonImages(initialOzonImages);
                setWbImages(initialWbImages);
                setLoading(false);

                // Загружаем изображения Ozon
                const ozonProducts = response.data.filter(p => p.ozonData);
                for (const product of ozonProducts) {
                    const productId = product.ozonData.product_id;
                    try {
                        await new Promise(resolve => setTimeout(resolve, 200));
                        const res = await axios.get(
                            `http://localhost:8080/api/ozon/products/${productId}/pictures`
                        );
                        
                        const item = res.data?.items?.[0];
                        const imageUrl = item?.primary_photo?.[0] || 
                                        item?.photo?.[0] || 
                                        (item?.photo_360?.length ? item.photo_360[0] : null);

                        if (imageUrl) {
                            setOzonImages(prev => ({
                                ...prev,
                                [productId]: imageUrl
                            }));
                        }
                    } catch (err) {
                        console.error(`Ошибка загрузки изображения Ozon для товара ${productId}:`, err);
                    } finally {
                        setImageLoading(prev => ({
                            ...prev,
                            [`ozon_${productId}`]: false
                        }));
                    }
                }

            } catch (err) {
                setError('Ошибка при загрузке данных: ' + err.message);
                setLoading(false);
            }
        };

        fetchData();
    }, []);

    const applyFilters = (productsToFilter, searchValue, platform) => {
        let filtered = [...productsToFilter];
        
        // Применяем поиск
        if (searchValue.trim() !== '') {
            const lowercasedSearch = searchValue.toLowerCase();
            filtered = filtered.filter(product => {
                const matchesVendorCode = product.vendorCode.toLowerCase().includes(lowercasedSearch);
                const ozonName = product.ozonData?.name?.toLowerCase() || '';
                const wbTitle = product.wbData?.title?.toLowerCase() || '';
                const matchesName = ozonName.includes(lowercasedSearch) || wbTitle.includes(lowercasedSearch);
                return matchesVendorCode || matchesName;
            });
        }
        
        // Применяем фильтр по платформе
        switch (platform) {
            case 'ozon':
                filtered = filtered.filter(product => !!product.ozonData);
                break;
            case 'wb':
                filtered = filtered.filter(product => !!product.wbData);
                break;
            case 'all':
            default:
                break;
        }
        
        setFilteredProducts(filtered);
    };

    useEffect(() => {
        applyFilters(products, searchTerm, platformFilter);
    }, [searchTerm, platformFilter, products]);

    const handlePlatformFilterChange = (platform) => {
        setPlatformFilter(platform);
    };

    const renderAvailability = (product) => {
        const hasOzon = !!product.ozonData;
        const hasWb = !!product.wbData;
        
        if (hasOzon && hasWb) {
            return <span style={{color: 'green'}}>Ozon + WB</span>;
        } else if (hasOzon) {
            return <span style={{color: 'green'}}>Ozon</span>;
        } else if (hasWb) {
            return <span style={{color: 'green'}}>WB</span>;
        } else {
            return <span style={{color: 'red'}}>Нет в наличии</span>;
        }
    };

    const calculateTotalStock = (product) => {
        let total = 0;

        if (product.ozonStock && !isNaN(product.ozonStock)) {
            total += parseInt(product.ozonStock);
        }

        if (product.wbData?.stocks?.length > 0) {
            product.wbData.stocks.forEach(stock => {
                const stockValue = parseInt(stock) || 0;
                total += stockValue;
            });
        }

        return total > 0 ? `${total} шт.` : '-';
    };

    const renderProductImage = (product) => {
        // Если есть изображение WB - используем его
        if (product.wbData?.imageUrl) {
            return (
                <img 
                    src={product.wbData.imageUrl} 
                    alt={product.wbData.title || product.ozonData?.name || ''}
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
            );
        }

        // Если нет WB, но есть Ozon - используем Ozon
        if (product.ozonData) {
            const productId = product.ozonData.product_id;
            const ozonImageUrl = ozonImages[productId];
            const isLoading = imageLoading[`ozon_${productId}`];

            if (isLoading) {
                return (
                    <div style={{ 
                        width: '80px', 
                        height: '80px', 
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center'
                    }}>
                        <div className="table-spinner"></div>
                    </div>
                );
            }

            if (ozonImageUrl) {
                return (
                    <img 
                        src={ozonImageUrl} 
                        alt={product.ozonData.name || ''}
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
                );
            }

            return (
                <div style={{ 
                    width: '80px', 
                    height: '80px', 
                    lineHeight: '80px',
                    textAlign: 'center',
                    color: '#999'
                }}>
                    Нет фото
                </div>
            );
        }

        // Если нет изображений вообще
        return (
            <div style={{ 
                width: '80px', 
                height: '80px', 
                lineHeight: '80px',
                textAlign: 'center',
                color: '#999'
            }}>
                -
            </div>
        );
    };

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
            
            <div style={{ 
                marginBottom: '20px',
                display: 'flex',
                alignItems: 'center',
                gap: '20px'
            }}>
                <div>
                    <input
                        type="text"
                        placeholder="Поиск по артикулу или названию..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        style={{
                            padding: '8px 12px',
                            width: '300px',
                            borderRadius: '4px',
                            border: '1px solid #ddd',
                            fontSize: '16px'
                        }}
                    />
                    {searchTerm && (
                        <span style={{ marginLeft: '10px', color: '#666' }}>
                            Найдено товаров: {filteredProducts.length}
                        </span>
                    )}
                </div>
                
                <div style={{ display: 'flex', gap: '10px' }}>
                    <button 
                        onClick={() => handlePlatformFilterChange('all')}
                        style={{
                            padding: '8px 16px',
                            backgroundColor: platformFilter === 'all' ? '#1890ff' : '#f0f0f0',
                            color: platformFilter === 'all' ? 'white' : 'black',
                            border: '1px solid #ddd',
                            borderRadius: '4px',
                            cursor: 'pointer'
                        }}
                    >
                        Все
                    </button>
                    <button 
                        onClick={() => handlePlatformFilterChange('ozon')}
                        style={{
                            padding: '8px 16px',
                            backgroundColor: platformFilter === 'ozon' ? '#1890ff' : '#f0f0f0',
                            color: platformFilter === 'ozon' ? 'white' : 'black',
                            border: '1px solid #ddd',
                            borderRadius: '4px',
                            cursor: 'pointer'
                        }}
                    >
                        Только Ozon
                    </button>
                    <button 
                        onClick={() => handlePlatformFilterChange('wb')}
                        style={{
                            padding: '8px 16px',
                            backgroundColor: platformFilter === 'wb' ? '#1890ff' : '#f0f0f0',
                            color: platformFilter === 'wb' ? 'white' : 'black',
                            border: '1px solid #ddd',
                            borderRadius: '4px',
                            cursor: 'pointer'
                        }}
                    >
                        Только WB
                    </button>
                </div>
            </div>
            
            <table style={{ borderCollapse: 'collapse', width: '100%' }}>
                <thead>
                    <tr style={{ backgroundColor: '#f2f2f2' }}>
                        <th style={{ border: '1px solid #ddd', padding: '8px' }}>Изображение</th>
                        <th style={{ border: '1px solid #ddd', padding: '8px' }}>Артикул</th>
                        <th style={{ border: '1px solid #ddd', padding: '8px' }}>Название</th>
                        <th style={{ border: '1px solid #ddd', padding: '8px' }}>Наличие</th>
                        <th style={{ border: '1px solid #ddd', padding: '8px' }}>Остатки WB</th>
                        <th style={{ border: '1px solid #ddd', padding: '8px' }}>Остатки Ozon</th>
                        <th style={{ border: '1px solid #ddd', padding: '8px' }}>Общие остатки</th>
                    </tr>
                </thead>
                <tbody>
                    {filteredProducts.map((product, index) => {
                        const name = product.wbData?.title || product.ozonData?.name || '-';
                        
                        return (
                            <tr key={index}>
                                <td style={{ border: '1px solid #ddd', padding: '8px', textAlign: 'center' }}>
                                    {renderProductImage(product)}
                                </td>
                                <td style={{ border: '1px solid #ddd', padding: '8px' }}>{product.vendorCode}</td>
                                <td style={{ border: '1px solid #ddd', padding: '8px' }}>{name}</td>
                                <td style={{ border: '1px solid #ddd', padding: '8px', textAlign: 'center' }}>
                                    {renderAvailability(product)}
                                </td>
                                <td style={{ border: '1px solid #ddd', padding: '8px' }}>
                                    {product.wbData ? (
                                        product.wbData.stocks && product.wbData.skus ? (
                                            product.wbData.skus.map((sku, i) => (
                                                <div key={sku}>
                                                    {product.wbData.stocks[i] || 0} шт.
                                                </div>
                                            ))
                                        ) : 'Загрузка...'
                                    ) : '-'}
                                </td>
                                <td style={{ border: '1px solid #ddd', padding: '8px' }}>
                                    {product.ozonStock > 0 ? `${product.ozonStock} шт.` : '-'}
                                </td>
                                <td style={{ 
                                    border: '1px solid #ddd', 
                                    padding: '8px',
                                    fontWeight: 'bold',
                                    color: calculateTotalStock(product) === '-' ? 'inherit' : '#1890ff'
                                }}>
                                    {calculateTotalStock(product)}
                                </td>
                            </tr>
                        );
                    })}
                </tbody>
            </table>
            
            {filteredProducts.length === 0 && (
                <div style={{ 
                    marginTop: '20px', 
                    padding: '20px', 
                    textAlign: 'center',
                    backgroundColor: '#f8f9fa',
                    borderRadius: '4px',
                    border: '1px solid #ddd'
                }}>
                    {searchTerm 
                        ? 'Товары не найдены. Попробуйте изменить условия поиска.'
                        : platformFilter === 'all'
                            ? 'Нет товаров для отображения'
                            : platformFilter === 'ozon'
                                ? 'Нет товаров на Ozon'
                                : 'Нет товаров на Wildberries'}
                </div>
            )}
        </div>
    );
};

export default ProductTable;
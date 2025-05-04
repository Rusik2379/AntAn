import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import ProductTable from './ProductTable';

const Product = () => {
  const navigate = useNavigate();

  useEffect(() => {
    if (!localStorage.getItem('token')) {
      navigate('/');
    }
  }, [navigate]);

  return (
    <div>
      <ProductTable />
    </div>
  );
};

export default Product;
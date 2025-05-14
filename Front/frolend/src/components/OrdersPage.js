// import React, { useState, useEffect } from 'react';

// const OrdersTable = () => {
//   const [orders, setOrders] = useState([]);
//   const [loading, setLoading] = useState(true);
//   const [error, setError] = useState(null);

//   useEffect(() => {
//     // const fetchOrders = async () => {
//     //   try {
//     //     const response = await fetch('http://localhost:8080/api/orders/merged', {
//     //       method: 'POST', // Явно указываем метод
//     //       headers: {
//     //         'Content-Type': 'application/json',
//     //       },
//     //       body: JSON.stringify({}), // Пустое тело, если не нужно передавать данные
//     //     });
//     //     if (!response.ok) {
//     //       throw new Error(`HTTP error! status: ${response.status}`);
//     //     }
//     //     const data = await response.json();
//     //     setOrders(data);
//     //   } catch (err) {
//     //     setError(err.message);
//     //   } finally {
//     //     setLoading(false);
//     //   }
//     // };

//     fetchOrders();
//   }, []);

//   if (loading) {
//     return <div>Loading...</div>;
//   }

//   if (error) {
//     return <div>Error: {error}</div>;
//   }

//   return (
//     <div className="orders-table-container">
//       <h2>Orders</h2>
//       <table className="orders-table">
//         <thead>
//           <tr>
//             <th>Image</th>
//             <th>Product Name</th>
//             <th>Source</th>
//             <th>Price</th>
//             <th>Quantity</th>
//           </tr>
//         </thead>
//         <tbody>
//           {orders.map((order, index) => (
//             <tr key={index}>
//               <td>
//                 {order.imageUrl ? (
//                   <img 
//                     src={order.imageUrl} 
//                     alt={order.productName || 'Product image'} 
//                     style={{ width: '50px', height: '50px', objectFit: 'cover' }}
//                   />
//                 ) : (
//                   'No image'
//                 )}
//               </td>
//               <td>{order.productName || 'N/A'}</td>
//               <td>{order.source}</td>
//               <td>{order.price}</td>
//               <td>{order.quantity}</td>
//             </tr>
//           ))}
//         </tbody>
//       </table>
//     </div>
//   );
// };

// export default OrdersTable;
import './Product'
import Product from './Product';
import Header from './Header';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';


function App() {
  return (
    <Router>
      <div className="App">
          <Header />
          <Routes>
              <Route path="/" element={<Product />} />
              {/* <Route path="/orders" element={<Orders />} />
              <Route path="/invoices" element={<Invoices />} />
              <Route path="/salaries" element={<Salaries />} />
              <Route path="/schedule" element={<Schedule />} /> */}
          </Routes>
      </div>
    </Router>
);
}

export default App;

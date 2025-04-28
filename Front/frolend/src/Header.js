import React from "react";
import ProductTable from "./ProductTable";
import { Link } from 'react-router-dom';

const Header = () => {
    return(
        <div>
            <header>
                <Link to="/">
                    <span className="header-item">Товары</span>
                </Link>
                <Link to="/orders">
                    <span className="header-item">Заказы</span>
                </Link>
                <Link to="/invoices">
                    <span className="header-item">Накладные</span>
                </Link>
                <Link to="/salaries">
                    <span className="header-item">Зарплаты</span>
                </Link>
                <Link to="/schedule">
                    <span className="header-item">Расписание</span>
                </Link>
            </header>            
        </div>)

}
export default Header;
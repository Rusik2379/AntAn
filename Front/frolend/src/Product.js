import React from "react";
import ProductTable from "./ProductTable";

const Product = () => {
    return(
        <div>
            <header>
                <a href="/"><span class="header-item">Товары</span></a>
                <a href="/orders"><span class="header-item">Заказы</span></a>
                <a href="/invoices"><span class="header-item">Накладные</span></a>
                <a href="/salaries"><span class="header-item">Зарплаты</span></a>
                <a href="/schedule"><span class="header-item">Расписание</span></a>
            </header>            
            <ProductTable />
        </div>)

}
export default Product;
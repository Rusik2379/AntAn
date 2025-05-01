package com.example.Diplom.DTO;

import lombok.Data;
import java.util.List;

@Data
public class OzonStockResponse {
    private List<Item> items;
    private String cursor;
    private int total;

    @Data
    public static class Item {
        private String offer_id;
        private Long product_id;
        private List<Stock> stocks;
    }

    @Data
    public static class Stock {
        private int present;
        private int reserved;
        private String type;
        private long sku; // Добавляем поле sku
        private String shipment_type; // Добавляем поле shipment_type
    }
}
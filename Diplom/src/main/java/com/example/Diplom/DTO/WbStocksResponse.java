package com.example.Diplom.DTO;

import lombok.Data;

import java.util.List;

@Data
public class WbStocksResponse {
    private List<Stock> stocks;

    @Data
    public static class Stock {
        private String sku;
        private Integer amount;
    }
}
package com.example.Diplom.DTO;

import lombok.Data;

import java.util.List;

@Data
public class WbStocksRequest {
    private List<String> skus;
}
// WbOrderResponse.java
package com.example.Diplom.DTO;

import lombok.Data;

import java.util.List;

@Data
public class WbOrderResponse {
    private Long orderId;
    private String createdAt;
    private List<String> skus;
    private Double price; // окончательная цена в рублях
    private Long nmId;
    private String productName;
    private String imageUrl;
}
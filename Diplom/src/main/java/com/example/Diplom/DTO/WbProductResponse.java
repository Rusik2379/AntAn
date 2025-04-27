package com.example.Diplom.DTO;

import lombok.Data;

import java.util.List;

@Data
public class WbProductResponse {
    private String vendorCode;
    private List<String> skus;
    private String updatedAt;
    private Long nmID;
    private String title;
}
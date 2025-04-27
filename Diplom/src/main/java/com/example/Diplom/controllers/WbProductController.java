package com.example.Diplom.controllers;

import com.example.Diplom.DTO.WbProductResponse;
import com.example.Diplom.services.WbApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wb")
public class WbProductController {

    private final WbApiService wbApiService;

    @Autowired
    public WbProductController(WbApiService wbApiService) {
        this.wbApiService = wbApiService;
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/wbproducts")
    public ResponseEntity<?> getWbProducts() {
        try {
            List<WbProductResponse> products = wbApiService.getProductList();
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Ошибка при обработке запроса: " + e.getMessage());
        }
    }
}
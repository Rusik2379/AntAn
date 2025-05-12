package com.example.Diplom.controllers;

import com.example.Diplom.DTO.WbOrderResponse;
import com.example.Diplom.DTO.WbProductResponse;
import com.example.Diplom.services.WbApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

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

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/orders/new")
    public ResponseEntity<?> getNewOrders() {
        try {
            List<WbOrderResponse> orders = wbApiService.getNewOrders();
            if (orders.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            }
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Ошибка при получении новых заказов",
                            "message", e.getMessage()
                    ));
        }
    }
}
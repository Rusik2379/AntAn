// OzonOrderController.java
package com.example.Diplom.controllers;

import com.example.Diplom.services.OzonApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ozon/orders")
public class OzonOrderController {

    private final OzonApiService ozonApiService;

    @Autowired
    public OzonOrderController(OzonApiService ozonApiService) {
        this.ozonApiService = ozonApiService;
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/new")
    public ResponseEntity<?> getNewOrders() {
        try {
            List<Map<String, Object>> orders = ozonApiService.getNewOrders();
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "error", "Ошибка при получении заказов Ozon",
                            "message", e.getMessage()
                    ));
        }
    }
}
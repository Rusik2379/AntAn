package com.example.Diplom.controllers;


import com.example.Diplom.services.OzonApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class ProductController {

    private final OzonApiService ozonApiService;

    @Autowired
    public ProductController(OzonApiService ozonApiService) {
        this.ozonApiService = ozonApiService;
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/")
    public String products() {
        return "products";
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/api/ozon/products")
    @ResponseBody
//    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<?> getOzonProducts() {
        try {
            ResponseEntity<String> response = ozonApiService.getProductList();

            if (!response.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.status(response.getStatusCode())
                        .body("Ошибка при запросе к Ozon API");
            }

            return response;
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Внутренняя ошибка сервера: " + e.getMessage());
        }
    }
}

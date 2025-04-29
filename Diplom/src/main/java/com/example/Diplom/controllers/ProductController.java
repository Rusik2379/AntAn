package com.example.Diplom.controllers;

import com.example.Diplom.DTO.WbProductResponse;
import com.example.Diplom.services.OzonApiService;
import com.example.Diplom.services.WbApiService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ProductController {

    private final OzonApiService ozonApiService;
    private final WbApiService wbApiService;
    private final ObjectMapper objectMapper;

    @Autowired
    public ProductController(OzonApiService ozonApiService, WbApiService wbApiService) {
        this.ozonApiService = ozonApiService;
        this.wbApiService = wbApiService;
        this.objectMapper = new ObjectMapper();
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/")
    public String products() {
        return "products";
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/api/products/merged")
    @ResponseBody
    public ResponseEntity<?> getMergedProducts() {
        try {
            // Получаем товары с обеих платформ
            ResponseEntity<String> ozonResponse = ozonApiService.getProductList();
            List<WbProductResponse> wbProducts = wbApiService.getProductList();

            if (!ozonResponse.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.status(ozonResponse.getStatusCode())
                        .body("Ошибка при запросе к Ozon API");
            }

            // Парсим товары Ozon
            JsonNode ozonProductsNode = objectMapper.readTree(ozonResponse.getBody()).path("result").path("items");
            List<Map<String, Object>> ozonProducts = new ArrayList<>();
            for (JsonNode productNode : ozonProductsNode) {
                Map<String, Object> product = new HashMap<>();
                product.put("offer_id", productNode.path("offer_id").asText());
                product.put("product_id", productNode.path("product_id").asText());
                product.put("name", productNode.path("name").asText(null)); // используем asText(null) чтобы вернуть null вместо "null"
                ozonProducts.add(product);
            }

            // Объединяем товары
            Map<String, Map<String, Object>> mergedProducts = new HashMap<>();

            // Добавляем товары Ozon
            for (Map<String, Object> ozonProduct : ozonProducts) {
                String offerId = (String) ozonProduct.get("offer_id");
                if (!mergedProducts.containsKey(offerId)) {
                    Map<String, Object> product = new HashMap<>();
                    product.put("vendorCode", offerId);
                    product.put("ozonData", ozonProduct);
                    product.put("wbData", null);
                    mergedProducts.put(offerId, product);
                }
            }

            // Добавляем товары Wildberries
            for (WbProductResponse wbProduct : wbProducts) {
                String vendorCode = wbProduct.getVendorCode();
                if (!mergedProducts.containsKey(vendorCode)) {
                    Map<String, Object> product = new HashMap<>();
                    product.put("vendorCode", vendorCode);
                    product.put("ozonData", null);
                    product.put("wbData", wbProduct);
                    mergedProducts.put(vendorCode, product);
                } else {
                    mergedProducts.get(vendorCode).put("wbData", wbProduct);
                }
            }

            // Для товаров Ozon без названия (которых нет на WB) получаем описание
            for (Map<String, Object> product : mergedProducts.values()) {
                Map<String, Object> ozonData = (Map<String, Object>) product.get("ozonData");
                WbProductResponse wbData = (WbProductResponse) product.get("wbData");

                if (ozonData != null && wbData == null && (ozonData.get("name") == null || ((String)ozonData.get("name")).isEmpty())) {
                    String productId = (String) ozonData.get("product_id");
                    try {
                        ResponseEntity<String> descriptionResponse = ozonApiService.getProductDescription(productId);
                        if (descriptionResponse.getStatusCode().is2xxSuccessful()) {
                            JsonNode descriptionNode = objectMapper.readTree(descriptionResponse.getBody());
                            String name = descriptionNode.path("result").path("name").asText(null);
                            if (name != null && !name.isEmpty()) {
                                ozonData.put("name", name);
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Ошибка при получении описания товара " + productId + ": " + e.getMessage());
                    }
                }
            }

            return ResponseEntity.ok(new ArrayList<>(mergedProducts.values()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Внутренняя ошибка сервера: " + e.getMessage());
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/api/ozon/products")
    @ResponseBody
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

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/api/ozon/products/{productId}/pictures")
    @ResponseBody
    public ResponseEntity<?> getOzonProductPictures(@PathVariable String productId) {
        try {
            ResponseEntity<String> response = ozonApiService.getProductPicturesInfo(productId);

            if (!response.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.status(response.getStatusCode())
                        .body("Ошибка при запросе изображений товара в Ozon API: " + response.getBody());
            }

            return response;
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Внутренняя ошибка сервера при получении изображений: " + e.getMessage());
        }
    }
}
package com.example.Diplom.controllers;

import com.example.Diplom.DTO.OzonStockResponse;
import com.example.Diplom.DTO.WbProductResponse;
import com.example.Diplom.services.OzonApiService;
import com.example.Diplom.services.WbApiService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*")
@Slf4j
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
            // 1. Получаем товары с Ozon
            ResponseEntity<String> ozonResponse = ozonApiService.getProductList();
            if (!ozonResponse.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.status(ozonResponse.getStatusCode())
                        .body("Ошибка при запросе к Ozon API: " + ozonResponse.getBody());
            }

            // 2. Получаем товары с Wildberries
            List<WbProductResponse> wbProducts;
            try {
                wbProducts = wbApiService.getProductList();
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Ошибка при получении товаров с Wildberries: " + e.getMessage());
            }

            // 3. Парсим товары Ozon
            List<Map<String, Object>> ozonProducts = new ArrayList<>();
            List<String> ozonOfferIds = new ArrayList<>();
            try {
                JsonNode ozonProductsNode = objectMapper.readTree(ozonResponse.getBody()).path("result").path("items");
                for (JsonNode productNode : ozonProductsNode) {
                    Map<String, Object> product = new HashMap<>();
                    String offerId = productNode.path("offer_id").asText();
                    product.put("offer_id", offerId);
                    product.put("product_id", productNode.path("product_id").asText());
                    product.put("name", productNode.path("name").asText(null));
                    ozonProducts.add(product);
                    ozonOfferIds.add(offerId);
                }
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Ошибка парсинга товаров Ozon: " + e.getMessage());
            }

            // 4. Получаем остатки Ozon (только если есть товары)
            Map<String, Integer> ozonStocks = new HashMap<>();
            if (!ozonOfferIds.isEmpty()) {
                try {
                    ResponseEntity<String> stocksResponse = ozonApiService.getProductStocks(ozonOfferIds);
                    if (stocksResponse.getStatusCode().is2xxSuccessful()) {
                        OzonStockResponse stocksData = objectMapper.readValue(stocksResponse.getBody(), OzonStockResponse.class);
                        if (stocksData.getItems() != null) {
                            for (OzonStockResponse.Item item : stocksData.getItems()) {
                                int totalStock = item.getStocks().stream()
                                        .filter(stock -> "fbs".equals(stock.getType())) // Учитываем только FBS остатки
                                        .mapToInt(stock -> stock.getPresent() - stock.getReserved())
                                        .sum();
                                ozonStocks.put(item.getOffer_id(), totalStock);
                            }
                        }
                    } else {
                        System.err.println("Ошибка при запросе остатков Ozon: " + stocksResponse.getStatusCode() + " - " + stocksResponse.getBody());
                    }
                } catch (Exception e) {
                    System.err.println("Ошибка при обработке остатков Ozon: " + e.getMessage());
                    e.printStackTrace();
                    // Не прерываем выполнение, просто оставляем пустые остатки
                }
            }

            // 5. Объединяем товары
            Map<String, Map<String, Object>> mergedProducts = new HashMap<>();

            // Добавляем товары Ozon
            for (Map<String, Object> ozonProduct : ozonProducts) {
                String offerId = (String) ozonProduct.get("offer_id");
                Map<String, Object> product = new HashMap<>();
                product.put("vendorCode", offerId);
                product.put("ozonData", ozonProduct);
                product.put("ozonStock", ozonStocks.getOrDefault(offerId, 0));
                product.put("wbData", null);
                mergedProducts.put(offerId, product);
            }

            // Добавляем товары Wildberries
            for (WbProductResponse wbProduct : wbProducts) {
                String vendorCode = wbProduct.getVendorCode();
                if (mergedProducts.containsKey(vendorCode)) {
                    mergedProducts.get(vendorCode).put("wbData", wbProduct);
                } else {
                    Map<String, Object> product = new HashMap<>();
                    product.put("vendorCode", vendorCode);
                    product.put("ozonData", null);
                    product.put("ozonStock", 0);
                    product.put("wbData", wbProduct);
                    mergedProducts.put(vendorCode, product);
                }
            }

            // 6. Получаем названия для товаров Ozon без названия
            for (Map<String, Object> product : mergedProducts.values()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> ozonData = (Map<String, Object>) product.get("ozonData");
                WbProductResponse wbData = (WbProductResponse) product.get("wbData");

                if (ozonData != null && wbData == null &&
                        (ozonData.get("name") == null || ((String)ozonData.get("name")).isEmpty())) {
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
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Внутренняя ошибка сервера: " + e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
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
            // Валидация productId
            if (productId == null || !productId.matches("\\d+")) {
                return ResponseEntity.badRequest()
                        .body("Invalid product ID format");
            }

            ResponseEntity<String> response = ozonApiService.getProductPicturesInfo(productId);

            if (!response.getStatusCode().is2xxSuccessful()) {
                String errorMsg = "Ошибка при запросе изображений товара в Ozon API: " +
                        response.getStatusCode() + " - " + response.getBody();
                System.err.println(errorMsg);
                return ResponseEntity.status(response.getStatusCode()).body(errorMsg);
            }

            // Проверяем, есть ли изображения в ответе
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            if (!rootNode.has("items") || rootNode.get("items").isEmpty()) {
                return ResponseEntity.ok().body("{\"message\": \"No images found for this product\"}");
            }

            return response;

        } catch (Exception e) {
            String errorMsg = "Внутренняя ошибка сервера при получении изображений: " + e.getMessage();
            System.err.println(errorMsg);
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(errorMsg);
        }
    }

    @CrossOrigin(origins = "http://localhost:3000", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
    @PostMapping("/api/orders/merged") // Измените с @GetMapping на @PostMapping
    @ResponseBody
    public ResponseEntity<?> getMergedOrders(@RequestBody(required = false) Map<String, Object> requestBody) {
        try {
            List<Map<String, Object>> wbOrders = getWbOrders();
            List<Map<String, Object>> ozonOrders = getOzonOrders();

            List<Map<String, Object>> mergedOrders = new ArrayList<>();
            mergedOrders.addAll(wbOrders);
            mergedOrders.addAll(ozonOrders);

            mergedOrders.sort(Comparator.comparing(
                    order -> (String) order.get("createdAt"),
                    Comparator.reverseOrder()
            ));

            return ResponseEntity.ok(mergedOrders);
        } catch (Exception e) {
            log.error("Error merging orders", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Ошибка при объединении заказов",
                            "message", e.getMessage()
                    ));
        }
    }

    private List<Map<String, Object>> getWbOrders() {
        try {
            return wbApiService.getNewOrders().stream()
                    .map(order -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("source", "wildberries");
                        map.put("orderId", order.getOrderId());
                        map.put("createdAt", order.getCreatedAt());
                        map.put("productName", order.getProductName());
                        map.put("quantity", 1);
                        map.put("price", order.getPrice());
                        map.put("imageUrl", order.getImageUrl());
                        return map;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to get WB orders", e);
            return Collections.emptyList();
        }
    }

    private List<Map<String, Object>> getOzonOrders() {
        try {
            return ozonApiService.getNewOrders();
        } catch (Exception e) {
            log.error("Failed to get Ozon orders", e);
            return Collections.emptyList();
        }
    }
}
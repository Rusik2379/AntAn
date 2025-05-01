package com.example.Diplom.services;

import com.example.Diplom.DTO.OzonProductListRequest;
import com.example.Diplom.DTO.OzonProductPicturesRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OzonApiService {

    @Value("${ozon.api.url}")
    private String apiUrl;

    @Value("${ozon.api.client-id}")
    private String clientId;

    @Value("${ozon.api.key}")
    private String apiKey;

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    public OzonApiService() {
        this.objectMapper = new ObjectMapper();
        this.restTemplate = new RestTemplate();
    }

    public ResponseEntity<String> getProductList() {
        HttpHeaders headers = createHeaders();

        OzonProductListRequest request = new OzonProductListRequest();
        request.setFilter(new OzonProductListRequest.Filter());
        request.getFilter().setVisibility("ALL");
        request.setLimit(1000);
        request.setLast_id("");

        HttpEntity<OzonProductListRequest> entity = new HttpEntity<>(request, headers);

        return restTemplate.exchange(
                apiUrl + "/v3/product/list",
                HttpMethod.POST,
                entity,
                String.class
        );
    }

    public ResponseEntity<String> getProductPicturesInfo(String productId) {
        HttpHeaders headers = createHeaders();

        try {
            OzonProductPicturesRequest request = new OzonProductPicturesRequest();
            request.setProduct_id(List.of(Long.parseLong(productId))); // Парсим ID как Long

            String requestBody = objectMapper.writeValueAsString(request);
            System.out.println("Request to Ozon pictures API: " + requestBody); // Логируем запрос

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl + "/v2/product/pictures/info",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            System.out.println("Response from Ozon pictures API: " + response.getStatusCode()); // Логируем ответ
            return response;

        } catch (NumberFormatException e) {
            System.err.println("Invalid product ID format: " + productId);
            return ResponseEntity.badRequest().body("Invalid product ID format");
        } catch (JsonProcessingException e) {
            System.err.println("Error creating request: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating request");
        } catch (Exception e) {
            System.err.println("Error getting pictures: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting pictures: " + e.getMessage());
        }
    }

    public ResponseEntity<String> getProductImage() {
        HttpHeaders headers = createHeaders();

        OzonProductListRequest request = new OzonProductListRequest();
        request.setFilter(new OzonProductListRequest.Filter());
        request.getFilter().setVisibility("ALL");
        request.setLimit(1000);
        request.setLast_id("");

        HttpEntity<OzonProductListRequest> entity = new HttpEntity<>(request, headers);

        return restTemplate.exchange(
                apiUrl + "/v1/product/pictures/import",
                HttpMethod.POST,
                entity,
                String.class
        );
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Client-Id", clientId);
        headers.set("Api-Key", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    public ResponseEntity<String> getProductDescription(String productId) {
        HttpHeaders headers = createHeaders();

        String requestBody = String.format("{\"product_id\": \"%s\"}", productId);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        return restTemplate.exchange(
                apiUrl + "/v1/product/info/description",
                HttpMethod.POST,
                entity,
                String.class
        );
    }

    public ResponseEntity<String> getProductStocks(List<String> offerIds) {
        HttpHeaders headers = createHeaders();

        try {
            // Создаем JSON запроса согласно документации Ozon
            Map<String, Object> requestMap = new HashMap<>();
            Map<String, Object> filterMap = new HashMap<>();
            filterMap.put("offer_id", offerIds);
            filterMap.put("visibility", "ALL");

            requestMap.put("filter", filterMap);
            requestMap.put("limit", 1000);

            String requestBody = objectMapper.writeValueAsString(requestMap);
            System.out.println("Sending request to Ozon stocks API: " + requestBody);

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            return restTemplate.exchange(
                    apiUrl + "/v4/product/info/stocks",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

        } catch (Exception e) {
            System.err.println("Error in getProductStocks: " + e.getMessage());
            throw new RuntimeException("Failed to get product stocks from Ozon", e);
        }
    }

}
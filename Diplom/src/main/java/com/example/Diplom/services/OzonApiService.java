package com.example.Diplom.services;

import com.example.Diplom.DTO.OzonOrderDTO;
import com.example.Diplom.DTO.OzonProductListRequest;
import com.example.Diplom.DTO.OzonProductPicturesRequest;
import com.example.Diplom.DTO.OzonStockResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OzonApiService {

    @Value("${ozon.api.url}")
    private String apiUrl;

    @Value("${ozon.api.client-id}")
    private String clientId;

    @Value("${ozon.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public OzonApiService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    // Метод для получения новых заказов
    public List<Map<String, Object>> getNewOrders() {
        try {
            HttpHeaders headers = createHeaders();
            String requestBody = buildOrderRequest();

            log.debug("Request headers: {}", headers);
            log.debug("Request body: {}", requestBody);

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<OzonOrderDTO> response = restTemplate.exchange(
                    apiUrl + "/v3/posting/fbs/list",
                    HttpMethod.POST,
                    entity,
                    OzonOrderDTO.class
            );

            if (response.getStatusCode() == HttpStatus.FORBIDDEN) {
                log.error("Access denied. Check API key permissions.");
                throw new RuntimeException("Доступ запрещен. Проверьте права API-ключа в личном кабинете Ozon");
            }

            if (response.getBody() == null) {
                throw new RuntimeException("Пустой ответ от API Ozon");
            }

            return processOzonOrders(response.getBody());
        } catch (HttpClientErrorException e) {
            log.error("Ozon API error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Ошибка API Ozon: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("Unexpected error", e);
            throw new RuntimeException("Неожиданная ошибка при запросе к Ozon API", e);
        }
    }

    private String buildOrderRequest() throws JsonProcessingException {
        Map<String, Object> request = new HashMap<>();
        request.put("dir", "ASC");
        request.put("limit", 100);
        request.put("offset", 0);

        Map<String, Object> filter = new HashMap<>();
        filter.put("since", OffsetDateTime.now().minusDays(7).format(DateTimeFormatter.ISO_DATE_TIME));
        filter.put("to", OffsetDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        filter.put("status", "awaiting_packaging");

        request.put("filter", filter);
        return objectMapper.writeValueAsString(request);
    }

    private List<Map<String, Object>> processOzonOrders(OzonOrderDTO response) {
        if (response.getResult() == null || response.getResult().getPostings() == null) {
            return Collections.emptyList();
        }

        return response.getResult().getPostings().stream()
                .flatMap(posting -> posting.getProducts().stream()
                        .map(product -> {
                            Map<String, Object> order = new HashMap<>();
                            order.put("source", "ozon");
                            order.put("orderId", posting.getPostingNumber());
                            order.put("createdAt", posting.getInProcessAt());
                            order.put("productName", product.getName());
                            order.put("quantity", product.getQuantity());
                            order.put("price", parsePrice(product.getPrice()));
                            order.put("offerId", product.getOfferId());
                            order.put("sku", product.getSku());
                            return order;
                        }))
                .collect(Collectors.toList());
    }

    private double parsePrice(String priceStr) {
        try {
            return Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            log.warn("Failed to parse price: {}", priceStr);
            return 0.0;
        }
    }

    // Остальные методы сервиса
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
            request.setProduct_id(List.of(Long.parseLong(productId)));

            String requestBody = objectMapper.writeValueAsString(request);
            log.debug("Request to Ozon pictures API: {}", requestBody);

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl + "/v2/product/pictures/info",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            log.debug("Response from Ozon pictures API: {}", response.getStatusCode());
            return response;

        } catch (Exception e) {
            log.error("Error getting product pictures", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting pictures: " + e.getMessage());
        }
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
            Map<String, Object> requestMap = new HashMap<>();
            Map<String, Object> filterMap = new HashMap<>();
            filterMap.put("offer_id", offerIds);
            filterMap.put("visibility", "ALL");

            requestMap.put("filter", filterMap);
            requestMap.put("limit", 1000);

            String requestBody = objectMapper.writeValueAsString(requestMap);
            log.debug("Sending request to Ozon stocks API: {}", requestBody);

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            return restTemplate.exchange(
                    apiUrl + "/v4/product/info/stocks",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

        } catch (Exception e) {
            log.error("Error getting product stocks", e);
            throw new RuntimeException("Failed to get product stocks from Ozon", e);
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Client-Id", clientId);
        headers.set("Api-Key", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }
}
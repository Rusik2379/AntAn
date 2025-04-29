package com.example.Diplom.services;

import com.example.Diplom.DTO.OzonProductListRequest;
import com.example.Diplom.DTO.OzonProductPicturesRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

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

        OzonProductPicturesRequest request = new OzonProductPicturesRequest();
        request.setProduct_id(List.of(productId));

        try {
            String requestBody = objectMapper.writeValueAsString(request);
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            return restTemplate.exchange(
                    apiUrl + "/v2/product/pictures/info",
                    HttpMethod.POST,
                    entity,
                    String.class
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to create request body", e);
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

}
package com.example.Diplom.services;

import com.example.Diplom.DTO.OzonProductListRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
public class OzonApiService {

    @Value("${ozon.api.url}")
    private String apiUrl;

    @Value("${ozon.api.client-id}")
    private String clientId;

    @Value("${ozon.api.key}")
    private String apiKey;

    private final String API_URL = "https://api-seller.ozon.ru";
    private final String CLIENT_ID = "2539576";
    private final String API_KEY = "49f38f21-d52d-4097-aca8-74415a8a86a4";

    public ResponseEntity<String> getProductList() {
        RestTemplate restTemplate = new RestTemplate();

        // Заголовки запроса
        HttpHeaders headers = new HttpHeaders();
        headers.set("Client-Id", CLIENT_ID);
        headers.set("Api-Key", API_KEY);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Тело запроса
        OzonProductListRequest request = new OzonProductListRequest();
        request.setFilter(new OzonProductListRequest.Filter());
        request.getFilter().setVisibility("ALL");
        request.setLimit(1000);
        request.setLast_id("");

        HttpEntity<OzonProductListRequest> entity = new HttpEntity<>(request, headers);

        // Отправка запроса
        return restTemplate.exchange(
                API_URL + "/v3/product/list",
                HttpMethod.POST,
                entity,
                String.class
        );
    }

    public ResponseEntity<String> getProductImage() {
        RestTemplate restTemplateImage = new RestTemplate();

        // Заголовки запроса
        HttpHeaders headers = new HttpHeaders();
        headers.set("Client-Id", CLIENT_ID);
        headers.set("Api-Key", API_KEY);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Тело запроса
        OzonProductListRequest request = new OzonProductListRequest();
        request.setFilter(new OzonProductListRequest.Filter());
        request.getFilter().setVisibility("ALL");
        request.setLimit(1000);
        request.setLast_id("");

        HttpEntity<OzonProductListRequest> entity = new HttpEntity<>(request, headers);

        // Отправка запроса
        return restTemplateImage.exchange(
                API_URL + "/v1/product/pictures/import",
                HttpMethod.POST,
                entity,
                String.class
        );
    }
}

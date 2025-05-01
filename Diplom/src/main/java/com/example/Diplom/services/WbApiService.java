package com.example.Diplom.services;

import com.example.Diplom.DTO.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class WbApiService {

    @Value("${wb.api.key}")
    private String apiKey;

    @Value("${wb.api.url}")
    private String apiUrl;

    @Value("${wb.warehouse.id}")
    private Integer warehouseId;

    public List<WbProductResponse> getProductList() {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        WbProductListRequest request = new WbProductListRequest();
        request.getSettings().getFilter().setWithPhoto(-1);
        request.getSettings().getCursor().setLimit(99);

        HttpEntity<WbProductListRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<WbApiResponse> response = restTemplate.exchange(
                apiUrl + "/content/v2/get/cards/list",
                HttpMethod.POST,
                entity,
                WbApiResponse.class
        );

        List<WbProductResponse> products = convertToResponse(response.getBody());

        // Получаем остатки для всех SKU
        Map<String, Integer> stocks = getStocksForSkus(
                products.stream()
                        .flatMap(p -> p.getSkus().stream())
                        .collect(Collectors.toList())
        );

        // Устанавливаем остатки для каждого продукта
        products.forEach(product -> {
            product.setStocks(
                    product.getSkus().stream()
                            .map(sku -> stocks.getOrDefault(sku, 0))
                            .collect(Collectors.toList())
            );
        });

        return products;
    }

    private Map<String, Integer> getStocksForSkus(List<String> skus) {
        if (skus.isEmpty()) {
            return Collections.emptyMap();
        }

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        WbStocksRequest request = new WbStocksRequest();
        request.setSkus(skus);

        HttpEntity<WbStocksRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<WbStocksResponse> response = restTemplate.exchange(
                    "https://marketplace-api.wildberries.ru/api/v3/stocks/" + warehouseId,
                    HttpMethod.POST,
                    entity,
                    WbStocksResponse.class
            );

            return response.getBody().getStocks().stream()
                    .collect(Collectors.toMap(
                            WbStocksResponse.Stock::getSku,
                            WbStocksResponse.Stock::getAmount
                    ));
        } catch (Exception e) {
            // В случае ошибки возвращаем пустую карту
            return Collections.emptyMap();
        }
    }

    private List<WbProductResponse> convertToResponse(WbApiResponse apiResponse) {
        return apiResponse.getCards().stream()
                .map(card -> {
                    WbProductResponse response = new WbProductResponse();
                    response.setVendorCode(card.getVendorCode());
                    response.setNmID(card.getNmID());
                    response.setUpdatedAt(card.getUpdatedAt());
                    response.setTitle(card.getTitle());

                    // Собираем все SKU из всех размеров
                    List<String> allSkus = card.getSizes().stream()
                            .flatMap(size -> size.getSkus().stream())
                            .collect(Collectors.toList());
                    response.setSkus(allSkus);

                    return response;
                })
                .collect(Collectors.toList());
    }
}
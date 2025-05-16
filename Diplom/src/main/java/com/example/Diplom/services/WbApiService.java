// WbApiService.java (дополненный)
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

    private final RestTemplate restTemplate = new RestTemplate();

    public List<WbProductResponse> getProductList() {
        List<WbProductResponse> allProducts = new ArrayList<>();
        WbApiResponse.Cursor nextCursor = null;
        int totalProcessed = 0;

        do {
            HttpHeaders headers = createHeaders();
            WbProductListRequest request = createRequest(nextCursor);

            HttpEntity<WbProductListRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<WbApiResponse> response = restTemplate.exchange(
                    apiUrl + "/content/v2/get/cards/list",
                    HttpMethod.POST,
                    entity,
                    WbApiResponse.class
            );

            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                throw new RuntimeException("Failed to get products from WB API. Status: " + response.getStatusCode());
            }

            WbApiResponse apiResponse = response.getBody();
            List<WbProductResponse> batchProducts = convertToProductResponse(apiResponse);
            allProducts.addAll(batchProducts);

            totalProcessed += batchProducts.size();
            nextCursor = apiResponse.getCursor();

        } while (nextCursor != null && !isLastBatch(nextCursor));

        // Получаем остатки для всех SKU
        Map<String, Integer> stocks = getStocksForSkus(
                allProducts.stream()
                        .flatMap(p -> p.getSkus().stream())
                        .collect(Collectors.toList())
        );

        // Устанавливаем остатки для каждого продукта
        allProducts.forEach(product -> {
            product.setStocks(
                    product.getSkus().stream()
                            .map(sku -> stocks.getOrDefault(sku, 0))
                            .collect(Collectors.toList())
            );
        });

        return allProducts;
    }

    private WbProductListRequest createRequest(WbApiResponse.Cursor cursor) {
        WbProductListRequest request = new WbProductListRequest();
        request.getSettings().getFilter().setWithPhoto(-1);
        request.getSettings().getCursor().setLimit(100); // Максимальный лимит

        if (cursor != null) {
            request.getSettings().getCursor().setUpdatedAt(cursor.getUpdatedAt());
            request.getSettings().getCursor().setNmID(cursor.getNmID());
        }

        return request;
    }

    private boolean isLastBatch(WbApiResponse.Cursor cursor) {
        // Если cursor.total меньше чем наш лимит (100), значит это последняя партия
        return cursor.getTotal() != null && cursor.getTotal() < 100;
    }

    public List<WbOrderResponse> getNewOrders() {
        HttpHeaders headers = createHeaders();
        HttpEntity<?> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<WbOrderApiResponse> response = restTemplate.exchange(
                    "https://marketplace-api.wildberries.ru/api/v3/orders/new",
                    HttpMethod.GET,
                    entity,
                    WbOrderApiResponse.class
            );

            return convertToOrderResponse(response.getBody());
        } catch (Exception e) {
            throw new RuntimeException("Failed to get new orders from WB API", e);
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private Map<String, Integer> getStocksForSkus(List<String> skus) {
        if (skus.isEmpty()) {
            return Collections.emptyMap();
        }

        HttpHeaders headers = createHeaders();

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
            return Collections.emptyMap();
        }
    }

    private List<WbProductResponse> convertToProductResponse(WbApiResponse apiResponse) {
        return apiResponse.getCards().stream()
                .map(card -> {
                    WbProductResponse response = new WbProductResponse();
                    response.setVendorCode(card.getVendorCode());
                    response.setNmID(card.getNmID());
                    response.setUpdatedAt(card.getUpdatedAt());
                    response.setTitle(card.getTitle());

                    // Устанавливаем URL изображения (первое доступное изображение)
                    if (card.getPhotos() != null && !card.getPhotos().isEmpty()) {
                        response.setImageUrl(card.getPhotos().get(0).getImageUrl());
                    }

                    // Собираем все SKU из всех размеров
                    List<String> allSkus = card.getSizes().stream()
                            .flatMap(size -> size.getSkus().stream())
                            .collect(Collectors.toList());
                    response.setSkus(allSkus);

                    return response;
                })
                .collect(Collectors.toList());
    }

    private List<WbOrderResponse> convertToOrderResponse(WbOrderApiResponse apiResponse) {
        if (apiResponse == null || apiResponse.getOrders() == null) {
            return Collections.emptyList();
        }

        List<WbProductResponse> allProducts = getProductList();
        Map<Long, WbProductResponse> productMap = allProducts.stream()
                .collect(Collectors.toMap(WbProductResponse::getNmID, p -> p));

        return apiResponse.getOrders().stream()
                .map(order -> {
                    WbOrderResponse response = new WbOrderResponse();
                    response.setOrderId(order.getId());
                    response.setCreatedAt(order.getCreatedAt());
                    response.setSkus(order.getSkus());

                    // Всегда используем convertedPrice, если он есть, иначе price
                    Integer priceInKopecks = order.getConvertedPrice() != null ?
                            order.getConvertedPrice() :
                            order.getPrice();

                    // Конвертируем в рубли
                    response.setPrice(priceInKopecks != null ? priceInKopecks / 100.0 : null);
                    response.setNmId(order.getNmId());

                    if (order.getNmId() != null && productMap.containsKey(order.getNmId())) {
                        WbProductResponse product = productMap.get(order.getNmId());
                        response.setProductName(product.getTitle());
                        response.setImageUrl(product.getImageUrl());
                    }

                    return response;
                })
                .collect(Collectors.toList());
    }
}
package com.example.Diplom.services;

import com.example.Diplom.DTO.WbApiResponse;
import com.example.Diplom.DTO.WbProductListRequest;
import com.example.Diplom.DTO.WbProductResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WbApiService {

    @Value("${wb.api.key}")
    private String apiKey;

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
                "https://content-api.wildberries.ru/content/v2/get/cards/list",
                HttpMethod.POST,
                entity,
                WbApiResponse.class
        );

        return convertToResponse(response.getBody());
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
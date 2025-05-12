// WbOrderApiResponse.java
package com.example.Diplom.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WbOrderApiResponse {
    @JsonProperty("orders")
    private List<Order> orders;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Order {
        @JsonProperty("id")
        private Long id;

        @JsonProperty("createdAt")
        private String createdAt;

        @JsonProperty("skus")
        private List<String> skus;

        @JsonProperty("price")
        private Integer price; // цена в копейках

        @JsonProperty("convertedPrice")
        private Integer convertedPrice; // цена в копейках

        @JsonProperty("nmId")
        private Long nmId;
    }
}
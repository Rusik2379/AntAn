package com.example.Diplom.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class WbApiResponse {
    @JsonProperty("cards")
    private List<WbProductCard> cards;

    @JsonProperty("cursor")
    private Cursor cursor; // Добавлено для пагинации

    @Data
    public static class Cursor {
        @JsonProperty("updatedAt")
        private String updatedAt;

        @JsonProperty("nmID")
        private Long nmID;

        @JsonProperty("total")
        private Integer total; // Добавлено для проверки последней партии
    }

    @Data
    public static class WbProductCard {
        @JsonProperty("vendorCode")
        private String vendorCode;

        @JsonProperty("nmID")
        private Long nmID;

        @JsonProperty("updatedAt")
        private String updatedAt;

        @JsonProperty("title")
        private String title;

        @JsonProperty("photos")
        private List<Photo> photos;

        @JsonProperty("sizes")
        private List<Size> sizes;

        @Data
        public static class Size {
            @JsonProperty("skus")
            private List<String> skus;
        }

        @Data
        public static class Photo {
            @JsonProperty("c246x328")
            private String imageUrl;
        }
    }
}
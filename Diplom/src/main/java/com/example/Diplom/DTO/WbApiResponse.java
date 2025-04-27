package com.example.Diplom.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class WbApiResponse {
    @JsonProperty("cards")
    private List<WbProductCard> cards;

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

        @JsonProperty("sizes")
        private List<Size> sizes;

        @Data
        public static class Size {
            @JsonProperty("skus")
            private List<String> skus;
        }
    }
}
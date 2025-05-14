// OzonOrderDTO.java
package com.example.Diplom.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class OzonOrderDTO {
    @JsonProperty("result")
    private Result result;

    @Data
    public static class Result {
        @JsonProperty("postings")
        private List<Posting> postings;
        @JsonProperty("hasNext")
        private Boolean hasNext;
    }

    @Data
    public static class Posting {
        @JsonProperty("posting_number")
        private String postingNumber;
        @JsonProperty("products")
        private List<Product> products;
        @JsonProperty("in_process_at")
        private String inProcessAt;
        @JsonProperty("status")
        private String status;
    }

    @Data
    public static class Product {
        @JsonProperty("name")
        private String name;
        @JsonProperty("quantity")
        private Integer quantity;
        @JsonProperty("price")
        private String price;
        @JsonProperty("offer_id")
        private String offerId;
        @JsonProperty("sku")
        private Long sku;
    }
}
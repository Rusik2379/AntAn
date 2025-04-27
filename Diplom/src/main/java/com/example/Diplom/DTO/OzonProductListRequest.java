package com.example.Diplom.DTO;

import lombok.Data;

@Data
public class OzonProductListRequest {
    private Filter filter;
    private String last_id;
    private int limit;

    @Data
    public static class Filter {
        private String visibility;
    }
}

package com.example.Diplom.DTO;

import lombok.Data;

@Data
public class WbProductListRequest {
    private Settings settings = new Settings();

    @Data
    public static class Settings {
        private Cursor cursor = new Cursor();
        private Filter filter = new Filter();
    }

    @Data
    public static class Cursor {
        private Integer limit;
    }

    @Data
    public static class Filter {
        private Integer withPhoto;
    }
}
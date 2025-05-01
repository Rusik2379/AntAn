package com.example.Diplom.DTO;

import lombok.Data;
import java.util.List;

@Data
public class OzonProductPicturesRequest {
    private List<Long> product_id; // Изменим на Long вместо String
    private String main_color_image; // Добавим опциональные поля
    private String color_image;
}
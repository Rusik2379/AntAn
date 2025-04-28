package com.example.Diplom.DTO;

import lombok.Data;
import java.util.List;

@Data
public class OzonProductPicturesRequest {
    private List<String> product_id; // Теперь принимаем список ID
}
package com.example.Diplom.auth.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShiftRequest {
    private Integer packages;
    private String date; // Формат: yyyy-MM-dd
}
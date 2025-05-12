package com.example.Diplom.auth.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class InvoiceRequest {
    private MultipartFile file;
    private String fileName;
}
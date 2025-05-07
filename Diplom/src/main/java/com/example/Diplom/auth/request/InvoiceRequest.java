package com.example.Diplom.auth.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class InvoiceRequest {
    private Long companyId;
    private MultipartFile pdfFile;
    private String fileName;
    private String fileType;
}
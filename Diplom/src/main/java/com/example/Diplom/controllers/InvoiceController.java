package com.example.Diplom.controllers;

import com.example.Diplom.models.Invoice;
import com.example.Diplom.services.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @Autowired
    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadInvoice(
            @RequestParam("file") MultipartFile file,
            @RequestParam String fileName,
            @RequestHeader("Authorization") String token) {

        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Файл не может быть пустым"));
            }

            if (!file.getContentType().equals("application/pdf")) {
                return ResponseEntity.badRequest().body(Map.of("message", "Только PDF файлы разрешены"));
            }

            invoiceService.saveInvoice(file, fileName, token);
            return ResponseEntity.ok(Map.of("message", "Накладная успешно загружена"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "Ошибка при загрузке файла: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Invoice>> getAllInvoices(
            @RequestHeader("Authorization") String token) {
        try {
            List<Invoice> invoices = invoiceService.getUserInvoices(token);
            return ResponseEntity.ok(invoices);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> downloadInvoice(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        try {
            Invoice invoice = invoiceService.getInvoiceById(id, token);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(invoice.getFileType()));
            headers.setContentDispositionFormData(
                    "attachment",
                    invoice.getFileName()
            );
            headers.setContentLength(invoice.getPdfFile().length);

            return new ResponseEntity<>(invoice.getPdfFile(), headers, HttpStatus.OK);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
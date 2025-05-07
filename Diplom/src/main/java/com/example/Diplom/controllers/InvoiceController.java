package com.example.Diplom.controllers;

import com.example.Diplom.auth.request.InvoiceRequest;
import com.example.Diplom.models.Invoice;
import com.example.Diplom.services.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadInvoice(@RequestParam("file") MultipartFile file,
                                                @RequestParam Long companyId,
                                                @RequestParam String fileName) {
        try {
            String fileType = file.getContentType();
            invoiceService.saveInvoice(file, companyId, fileName, fileType);
            return ResponseEntity.ok("PDF файл успешно загружен");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при загрузке файла: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Invoice>> getAllInvoices() {
        List<Invoice> invoices = invoiceService.getAllInvoices();
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getInvoice(@PathVariable Long id) {
        Invoice invoice = invoiceService.getInvoice(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(invoice.getFileType()));
        headers.setContentDispositionFormData("attachment", invoice.getFileName());

        return new ResponseEntity<>(invoice.getPdfFile(), headers, HttpStatus.OK);
    }
}
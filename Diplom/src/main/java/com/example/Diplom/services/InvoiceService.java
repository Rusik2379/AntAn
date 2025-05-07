package com.example.Diplom.services;

import com.example.Diplom.models.Company;
import com.example.Diplom.models.Invoice;
import com.example.Diplom.repositories.CompanyRepository;
import com.example.Diplom.repositories.InvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private CompanyRepository companyRepository;

    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    public Invoice saveInvoice(MultipartFile file, Long companyId, String fileName, String fileType) throws IOException {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        Invoice invoice = new Invoice();
        invoice.setCompany(company);
        invoice.setPdfFile(file.getBytes());
        invoice.setFileName(fileName);
        invoice.setFileType(fileType);
        invoice.setDate(LocalDateTime.now());

        return invoiceRepository.save(invoice);
    }

    public Invoice getInvoice(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
    }
}
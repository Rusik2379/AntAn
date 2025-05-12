package com.example.Diplom.services;

import com.example.Diplom.models.Company;
import com.example.Diplom.models.Invoice;
import com.example.Diplom.models.User;
import com.example.Diplom.repositories.CompanyRepository;
import com.example.Diplom.repositories.InvoiceRepository;
import com.example.Diplom.repositories.UserRepository;
import com.example.Diplom.security.JwtService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    public InvoiceService(InvoiceRepository invoiceRepository,
                          JwtService jwtService,
                          UserRepository userRepository,
                          CompanyRepository companyRepository) {
        this.invoiceRepository = invoiceRepository;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
    }

    @Transactional
    public void saveInvoice(MultipartFile file, String fileName, String token)
            throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Файл не может быть пустым");
        }

        Long companyId = extractCompanyIdFromToken(token);
        User user = getUserFromToken(token);
        Company company = getCompany(companyId, user);

        Invoice invoice = new Invoice();
        invoice.setCompany(company);
        invoice.setDate(LocalDateTime.now());
        invoice.setFileName(fileName);
        invoice.setPdfFile(file.getBytes());
        invoice.setFileType(file.getContentType());

        invoiceRepository.save(invoice);
    }

    public List<Invoice> getUserInvoices(String token) {
        Long companyId = extractCompanyIdFromToken(token);
        return invoiceRepository.findByCompanyId(companyId);
    }

    public Invoice getInvoiceById(Long id, String token) {
        Long companyId = extractCompanyIdFromToken(token);
        return invoiceRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new RuntimeException("Накладная не найдена или доступ запрещен"));
    }

    private Long extractCompanyIdFromToken(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Неверный токен авторизации");
        }
        String cleanedToken = token.substring(7).trim();
        try {
            return jwtService.extractCompanyId(cleanedToken);
        } catch (Exception e) {
            throw new IllegalArgumentException("Неверный токен авторизации");
        }
    }

    private User getUserFromToken(String token) {
        Long userId = jwtService.extractUserId(token.substring(7).trim());
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }

    private Company getCompany(Long companyId, User user) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Компания не найдена"));

        if (!user.getCompany().getId().equals(companyId)) {
            throw new RuntimeException("Пользователь не имеет доступа к этой компании");
        }

        return company;
    }
}
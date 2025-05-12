package com.example.Diplom.repositories;

import com.example.Diplom.models.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByCompanyId(Long companyId);
    Optional<Invoice> findByIdAndCompanyId(Long id, Long companyId);
}
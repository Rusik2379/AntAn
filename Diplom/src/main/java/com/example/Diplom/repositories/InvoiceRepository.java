package com.example.Diplom.repositories;

import com.example.Diplom.models.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    @Query("SELECT i FROM Invoice i WHERE i.company.id = :companyId")
    List<Invoice> findByCompanyId(@Param("companyId") Long companyId);

    @Query("SELECT i FROM Invoice i WHERE i.id = :id AND i.company.id = :companyId")
    Optional<Invoice> findByIdAndCompanyId(@Param("id") Long id, @Param("companyId") Long companyId);
}
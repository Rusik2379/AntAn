package com.example.Diplom.repositories;

import com.example.Diplom.models.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository

public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByName(String name);  // Изменено с findByCompanyname
}
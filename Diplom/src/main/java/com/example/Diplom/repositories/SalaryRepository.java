package com.example.Diplom.repositories;

import com.example.Diplom.models.Salery;
import com.example.Diplom.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalaryRepository extends JpaRepository<Salery, Long> {
    List<Salery> findByUserAndPaid(User user, String paid);
    List<Salery> findByUser(User user);
}
package com.example.Diplom.repositories;

import com.example.Diplom.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findById(Long id);
    boolean existsByEmail(String email);

    // Измененный метод поиска
    List<User> findByCompanyName(String companyName); // Поиск по company_name в User

    interface UserProjection {
        Long getId();
        String getEmail();
        String getCompanyName();
        String getFirstName();
        String getLastName();
    }
}
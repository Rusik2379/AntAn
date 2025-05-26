package com.example.Diplom.repositories;

import com.example.Diplom.models.WorkWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkWeekRepository extends JpaRepository<WorkWeek, Long> {
    Optional<WorkWeek> findByStartDateAndCompanyId(LocalDate startDate, Long companyId);
    List<WorkWeek> findByCompanyIdOrderByStartDateDesc(Long companyId);

    @Query("SELECT w FROM WorkWeek w WHERE w.startDate = :startDate")
    Optional<WorkWeek> findByStartDate(@Param("startDate") LocalDate startDate);
}
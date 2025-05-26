package com.example.Diplom.repositories;

import com.example.Diplom.models.WorkDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkDayRepository extends JpaRepository<WorkDay, Long> {
    Optional<WorkDay> findByDateAndWorkWeekCompanyId(LocalDate date, Long companyId);
    List<WorkDay> findByWorkWeekId(Long workWeekId);
    List<WorkDay> findByWorkersIdAndDateBetween(Long userId, LocalDate start, LocalDate end);

    @Query("SELECT d FROM WorkDay d WHERE d.date = :date")
    Optional<WorkDay> findByDate(@Param("date") LocalDate date);
}
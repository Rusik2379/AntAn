package com.example.Diplom.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "work_weeks")
public class WorkWeek {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate startDate; // Дата понедельника недели

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @OneToMany(mappedBy = "workWeek", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default // Это важно - инициализирует список при создании через builder
    private List<WorkDay> workDays = new ArrayList<>();

    @Column(nullable = false)
    private boolean published; // Опубликовано ли расписание

    @Column(nullable = false)
    private boolean locked; // Заблокировано ли для изменений

    public void addWorkDay(WorkDay workDay) {
        if (workDays == null) {
            workDays = new ArrayList<>();
        }
        workDays.add(workDay);
        workDay.setWorkWeek(this);
    }
}
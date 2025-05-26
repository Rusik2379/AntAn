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
@Table(name = "work_days")
public class WorkDay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "week_id")
    private WorkWeek workWeek;

    @ManyToMany
    @JoinTable(
            name = "work_day_users",
            joinColumns = @JoinColumn(name = "work_day_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private List<User> workers = new ArrayList<>();

    @Column(nullable = false)
    private int maxWorkers; // Максимальное количество работников в день

    public boolean isFull() {
        return workers.size() >= maxWorkers;
    }

    public boolean hasWorker(User user) {
        return workers.stream().anyMatch(u -> u.getId().equals(user.getId()));
    }
}
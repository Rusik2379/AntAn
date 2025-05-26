package com.example.Diplom.DTO;

import com.example.Diplom.models.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkDayDTO {
    private Long id;
    private LocalDate date;
    private int maxWorkers;
    @Builder.Default
    private List<User> workers = List.of(); // Изменил на неизменяемый список по умолчанию
    private boolean isFull;
}
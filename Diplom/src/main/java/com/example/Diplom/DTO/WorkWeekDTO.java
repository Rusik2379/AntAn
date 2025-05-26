package com.example.Diplom.DTO;

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
public class WorkWeekDTO {
    private Long id;
    private LocalDate startDate;
    private boolean published;
    private boolean locked;
    private List<WorkDayDTO> workDays;
}
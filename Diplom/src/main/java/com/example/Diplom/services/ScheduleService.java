package com.example.Diplom.services;

import com.example.Diplom.DTO.WorkDayDTO;
import com.example.Diplom.DTO.WorkWeekDTO;
import com.example.Diplom.models.*;
import com.example.Diplom.repositories.CompanyRepository;
import com.example.Diplom.repositories.UserRepository;
import com.example.Diplom.repositories.WorkDayRepository;
import com.example.Diplom.repositories.WorkWeekRepository;
import com.example.Diplom.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final WorkWeekRepository workWeekRepository;
    private final WorkDayRepository workDayRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final CompanyRepository companyRepository;

    private static final int MAX_WORKERS_PER_DAY = 3;

    @Transactional
    public WorkWeekDTO getOrCreateWorkWeek(LocalDate anyDateInWeek, Long companyId) {
        LocalDate monday = anyDateInWeek.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        Optional<WorkWeek> existingWeek;
        if (companyId != null) {
            existingWeek = workWeekRepository.findByStartDateAndCompanyId(monday, companyId);
        } else {
            existingWeek = workWeekRepository.findByStartDate(monday);
        }

        if (existingWeek.isPresent()) {
            return convertToDTO(existingWeek.get());
        }

        WorkWeek newWeek = WorkWeek.builder()
                .startDate(monday)
                .published(false)
                .locked(false)
                .build();

        if (companyId != null) {
            Company company = companyRepository.getReferenceById(companyId);
            newWeek.setCompany(company);
        }

        for (int i = 0; i < 7; i++) {
            LocalDate dayDate = monday.plusDays(i);
            WorkDay workDay = WorkDay.builder()
                    .date(dayDate)
                    .maxWorkers(MAX_WORKERS_PER_DAY)
                    .workers(new ArrayList<>())
                    .build();
            newWeek.addWorkDay(workDay);
        }

        WorkWeek savedWeek = workWeekRepository.save(newWeek);
        return convertToDTO(savedWeek);
    }

    @Transactional
    public WorkDayDTO assignWorkerToDay(LocalDate date, Long userId, Long companyId) {
        Optional<WorkDay> workDayOptional = companyId != null
                ? workDayRepository.findByDateAndWorkWeekCompanyId(date, companyId)
                : workDayRepository.findByDate(date);
        WorkDay workDay = workDayOptional.orElseThrow(() -> new RuntimeException("Work day not found"));

        if (workDay.getWorkWeek().isLocked()) {
            throw new RuntimeException("This week is locked for changes");
        }

        if (workDay.isFull()) {
            throw new RuntimeException("This day is already full");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (workDay.hasWorker(user)) {
            throw new RuntimeException("User is already assigned to this day");
        }

        workDay.getWorkers().add(user);
        WorkDay savedDay = workDayRepository.save(workDay);
        return convertToDTO(savedDay);
    }

    @Transactional
    public WorkDayDTO removeWorkerFromDay(LocalDate date, Long userId, Long companyId) {
        Optional<WorkDay> workDayOptional = companyId != null
                ? workDayRepository.findByDateAndWorkWeekCompanyId(date, companyId)
                : workDayRepository.findByDate(date);
        WorkDay workDay = workDayOptional.orElseThrow(() -> new RuntimeException("Work day not found"));

        if (workDay.getWorkWeek().isLocked()) {
            throw new RuntimeException("This week is locked for changes");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!workDay.hasWorker(user)) {
            throw new RuntimeException("User is not assigned to this day");
        }

        workDay.getWorkers().removeIf(u -> u.getId().equals(userId));
        WorkDay savedDay = workDayRepository.save(workDay);
        return convertToDTO(savedDay);
    }

    @Transactional
    public void clearWeek(LocalDate anyDateInWeek, Long companyId) {
        LocalDate monday = anyDateInWeek.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        Optional<WorkWeek> workWeekOptional = companyId != null
                ? workWeekRepository.findByStartDateAndCompanyId(monday, companyId)
                : workWeekRepository.findByStartDate(monday);
        WorkWeek workWeek = workWeekOptional.orElseThrow(() -> new RuntimeException("Work week not found"));

        if (workWeek.isLocked()) {
            throw new RuntimeException("This week is locked for changes");
        }

        workWeek.getWorkDays().forEach(day -> day.getWorkers().clear());
        workWeekRepository.save(workWeek);
    }

    @Transactional
    public WorkWeekDTO publishWeek(LocalDate anyDateInWeek, Long companyId) {
        LocalDate monday = anyDateInWeek.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        Optional<WorkWeek> workWeekOptional = companyId != null
                ? workWeekRepository.findByStartDateAndCompanyId(monday, companyId)
                : workWeekRepository.findByStartDate(monday);
        WorkWeek workWeek = workWeekOptional.orElseThrow(() -> new RuntimeException("Work week not found"));

        workWeek.setPublished(true);
        workWeek.setLocked(true);

        WorkWeek savedWeek = workWeekRepository.save(workWeek);
        return convertToDTO(savedWeek);
    }

    public List<WorkDayDTO> getUserSchedule(Long userId, LocalDate startDate, LocalDate endDate) {
        List<WorkDay> workDays = workDayRepository.findByWorkersIdAndDateBetween(userId, startDate, endDate);
        return workDays.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private WorkWeekDTO convertToDTO(WorkWeek workWeek) {
        List<WorkDayDTO> dayDTOs = workWeek.getWorkDays().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return WorkWeekDTO.builder()
                .id(workWeek.getId())
                .startDate(workWeek.getStartDate())
                .published(workWeek.isPublished())
                .locked(workWeek.isLocked())
                .workDays(dayDTOs)
                .build();
    }

    private WorkDayDTO convertToDTO(WorkDay workDay) {
        return WorkDayDTO.builder()
                .id(workDay.getId())
                .date(workDay.getDate())
                .maxWorkers(workDay.getMaxWorkers())
                .workers(List.copyOf(workDay.getWorkers()))
                .isFull(workDay.isFull())
                .build();
    }
}
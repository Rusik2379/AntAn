package com.example.Diplom.controllers;

import com.example.Diplom.DTO.WorkDayDTO;
import com.example.Diplom.DTO.WorkWeekDTO;
import com.example.Diplom.security.JwtService;
import com.example.Diplom.services.ScheduleService;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000",
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST,
                RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS},
        allowCredentials = "true")
public class ScheduleController {
    private final ScheduleService scheduleService;
    private final JwtService jwtService;

    @GetMapping
    public ResponseEntity<WorkWeekDTO> getWorkWeek(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "").trim();
            String username = jwtService.extractUsername(token);
            if (username == null || !jwtService.isTokenValid(token, jwtService.loadUserByUsername(username))) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            Long companyId = jwtService.extractCompanyId(token); // Может быть null
            WorkWeekDTO week = scheduleService.getOrCreateWorkWeek(date, companyId);
            return ResponseEntity.ok(week);
        } catch (JwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("Error processing schedule request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/assign")
    public ResponseEntity<WorkDayDTO> assignToDay(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "").trim();
            String username = jwtService.extractUsername(token);
            if (username == null || !jwtService.isTokenValid(token, jwtService.loadUserByUsername(username))) {
                log.warn("Unauthorized access attempt with invalid token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            Long userId = jwtService.extractUserId(token);
            Long companyId = jwtService.extractCompanyId(token);

            if (userId == null) {
                log.error("User ID is null in token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // companyId может быть null, ScheduleService это обрабатывает
            log.info("Assigning userId: {} to date: {}, companyId: {}", userId, date, companyId);
            WorkDayDTO day = scheduleService.assignWorkerToDay(date, userId, companyId);
            return ResponseEntity.ok(day);

        } catch (JwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (RuntimeException e) {
            log.error("Error assigning to day: {}", e.getMessage());
            if (e.getMessage().equals("Work day not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            log.error("Unexpected error assigning to day", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/remove")
    public ResponseEntity<WorkDayDTO> removeFromDay(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestHeader("Authorization") String authHeader) {

        try {
            String token = authHeader.replace("Bearer ", "").trim();
            Long userId = jwtService.extractUserId(token);
            Long companyId = jwtService.extractCompanyId(token);

            if (userId == null || companyId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            WorkDayDTO day = scheduleService.removeWorkerFromDay(date, userId, companyId);
            return ResponseEntity.ok(day);

        } catch (Exception e) {
            log.error("Error removing from day: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearWeek(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestHeader("Authorization") String authHeader) {

        try {
            String token = authHeader.replace("Bearer ", "").trim();
            Long companyId = jwtService.extractCompanyId(token);

            if (companyId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            scheduleService.clearWeek(date, companyId);
            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            log.error("Error clearing week: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/publish")
    public ResponseEntity<WorkWeekDTO> publishWeek(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestHeader("Authorization") String authHeader) {

        try {
            String token = authHeader.replace("Bearer ", "").trim();
            Long companyId = jwtService.extractCompanyId(token);

            if (companyId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            WorkWeekDTO week = scheduleService.publishWeek(date, companyId);
            return ResponseEntity.ok(week);

        } catch (Exception e) {
            log.error("Error publishing week: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/my-schedule")
    public ResponseEntity<List<WorkDayDTO>> getUserSchedule(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestHeader("Authorization") String authHeader) {

        try {
            String token = authHeader.replace("Bearer ", "").trim();
            Long userId = jwtService.extractUserId(token);

            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            List<WorkDayDTO> schedule = scheduleService.getUserSchedule(userId, startDate, endDate);
            return ResponseEntity.ok(schedule);

        } catch (Exception e) {
            log.error("Error getting user schedule: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }


    @GetMapping("/all-schedule")
    public ResponseEntity<List<WorkDayDTO>> getAllSchedule(
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "").trim();
            String username = jwtService.extractUsername(token);
            if (username == null || !jwtService.isTokenValid(token, jwtService.loadUserByUsername(username))) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            Long companyId = jwtService.extractCompanyId(token);

            List<WorkDayDTO> allSchedule = scheduleService.getAllSchedule(companyId);
            return ResponseEntity.ok(allSchedule);
        } catch (JwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("Error processing all schedule request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
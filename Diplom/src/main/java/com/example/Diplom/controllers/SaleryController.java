package com.example.Diplom.controllers;

import com.example.Diplom.models.Salery;
import com.example.Diplom.auth.request.ShiftRequest;
import com.example.Diplom.services.SalaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/salaries")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class SaleryController {
    private final SalaryService saleryService;
    private final SalaryService salaryService;

    @GetMapping
    public ResponseEntity<List<Salery>> getUserSalaries(@RequestHeader("Authorization") String token) {
        try {
            return ResponseEntity.ok(salaryService.getUserSalaries(token));
        } catch (RuntimeException e) {
            if (e.getMessage().equals("User not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    @PatchMapping("/{id}/pay")
    public ResponseEntity<Salery> markSalaryAsPaid(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        try {
            return ResponseEntity.ok(saleryService.markSalaryAsPaid(id, token));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/add")
    public ResponseEntity<Salery> addShift(
            @RequestBody ShiftRequest shiftRequest,
            @RequestHeader("Authorization") String token) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(saleryService.addShift(shiftRequest, token));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/claim-all")
    public ResponseEntity<Void> claimAllSalaries(@RequestHeader("Authorization") String token) {
        try {
            saleryService.claimAllSalaries(token);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
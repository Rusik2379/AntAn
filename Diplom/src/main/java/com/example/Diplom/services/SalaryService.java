package com.example.Diplom.services;

import com.example.Diplom.models.Salery;
import com.example.Diplom.auth.request.ShiftRequest;
import com.example.Diplom.models.User;
import com.example.Diplom.repositories.SaleryRepository;
import com.example.Diplom.repositories.UserRepository;
import com.example.Diplom.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalaryService {
    private final SaleryRepository salaryRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public List<Salery> getUserSalaries(String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "").trim();
            String email = jwtService.extractUsername(token);

            if (email == null) {
                throw new RuntimeException("Invalid token");
            }

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            return salaryRepository.findByUserAndPaid(user, "No");

        } catch (Exception e) {
            log.error("Error fetching salaries: {}", e.getMessage());
            throw new RuntimeException("Authentication failed", e);
        }
    }

    public Salery markSalaryAsPaid(Long salaryId, String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "").trim();
            String email = jwtService.extractUsername(token);

            if (email == null) {
                throw new RuntimeException("Invalid token");
            }

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Salery salary = salaryRepository.findById(salaryId)
                    .orElseThrow(() -> new RuntimeException("Salary not found"));

            if (!salary.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("This salary doesn't belong to the current user");
            }

            salary.setPaid("Yes");
            return salaryRepository.save(salary);

        } catch (Exception e) {
            log.error("Error marking salary as paid: {}", e.getMessage());
            throw new RuntimeException("Authentication failed", e);
        }
    }

    public Salery addShift(ShiftRequest shiftRequest, String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "").trim();
            String email = jwtService.extractUsername(token);

            if (email == null) {
                throw new RuntimeException("Invalid token");
            }

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Salery salary = new Salery();
            salary.setUser(user);
            salary.setDate(LocalDateTime.parse(shiftRequest.getDate() + "T00:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            salary.setSum(shiftRequest.getPackages() * 5.0);
            salary.setRate(5.0);
            salary.setPaid("No");

            return salaryRepository.save(salary);

        } catch (Exception e) {
            log.error("Error adding shift: {}", e.getMessage());
            throw new RuntimeException("Authentication failed", e);
        }
    }

    public void claimAllSalaries(String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "").trim();
            String email = jwtService.extractUsername(token);

            if (email == null) {
                throw new RuntimeException("Invalid token");
            }

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<Salery> unpaidSalaries = salaryRepository.findByUserAndPaid(user, "No");
            for (Salery salary : unpaidSalaries) {
                salary.setPaid("Yes");
                salaryRepository.save(salary);
            }

        } catch (Exception e) {
            log.error("Error claiming all salaries: {}", e.getMessage());
            throw new RuntimeException("Authentication failed", e);
        }
    }
}
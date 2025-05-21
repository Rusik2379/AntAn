package com.example.Diplom.services;

import com.example.Diplom.models.Salery;
import com.example.Diplom.auth.request.ShiftRequest;
import com.example.Diplom.models.User;
import com.example.Diplom.repositories.SaleryRepository;
import com.example.Diplom.repositories.UserRepository;
import com.example.Diplom.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SaleryService {
    private static final Logger logger = LoggerFactory.getLogger(SaleryService.class);
    private final SaleryRepository saleryRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public List<Salery> getUserSalaries(String token) {
        logger.info("Received token: {}", token);
        try {
            String cleanToken = token.replace("Bearer ", "").trim();
            String email = jwtService.extractUserName(cleanToken);
            logger.info("Extracted email: {}", email);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            logger.info("Found user: {}", user.getId());
            List<Salery> salaries = saleryRepository.findByUserAndPaid(user, "No");
            logger.info("Found salaries: {}", salaries.size());
            return salaries;
        } catch (Exception e) {
            logger.error("Error fetching salaries: {}", e.getMessage(), e);
            throw e;
        }
    }

    public Salery markSalaryAsPaid(Long salaryId, String token) {
        logger.info("Received token for markSalaryAsPaid: {}", token);
        try {
            String cleanToken = token.replace("Bearer ", "").trim();
            String email = jwtService.extractUserName(cleanToken);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Salery salary = saleryRepository.findById(salaryId)
                    .orElseThrow(() -> new RuntimeException("Salary not found"));

            if (!salary.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("This salary doesn't belong to the current user");
            }

            salary.setPaid("Yes");
            return saleryRepository.save(salary);
        } catch (Exception e) {
            logger.error("Error marking salary as paid: {}", e.getMessage(), e);
            throw e;
        }
    }

    public Salery addShift(ShiftRequest shiftRequest, String token) {
        logger.info("Received token for addShift: {}", token);
        try {
            String cleanToken = token.replace("Bearer ", "").trim();
            String email = jwtService.extractUserName(cleanToken);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Salery salary = new Salery();
            salary.setUser(user);
            salary.setDate(LocalDateTime.parse(shiftRequest.getDate() + "T00:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            salary.setSum(shiftRequest.getPackages() * 5.0);
            salary.setRate(5.0);
            salary.setPaid("No");

            return saleryRepository.save(salary);
        } catch (Exception e) {
            logger.error("Error adding shift: {}", e.getMessage(), e);
            throw e;
        }
    }

    public void claimAllSalaries(String token) {
        logger.info("Received token for claimAllSalaries: {}", token);
        try {
            String cleanToken = token.replace("Bearer ", "").trim();
            String email = jwtService.extractUserName(cleanToken);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<Salery> unpaidSalaries = saleryRepository.findByUserAndPaid(user, "No");
            for (Salery salary : unpaidSalaries) {
                salary.setPaid("Yes");
                saleryRepository.save(salary);
            }
            logger.info("Marked {} salaries as paid for user: {}", unpaidSalaries.size(), user.getId());
        } catch (Exception e) {
            logger.error("Error claiming all salaries: {}", e.getMessage(), e);
            throw e;
        }
    }
}
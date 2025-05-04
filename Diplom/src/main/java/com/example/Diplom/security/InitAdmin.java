package com.example.Diplom.security;

import com.example.Diplom.models.Company;
import com.example.Diplom.models.User;
import com.example.Diplom.repositories.CompanyRepository;
import com.example.Diplom.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import static com.example.Diplom.models.enums.Role.ROLE_ADMIN;

@Slf4j
@Component
@RequiredArgsConstructor
public class InitAdmin implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            log.info("Пользователи не найдены, пользователь с правами администратора создан");

            Company company = Company.builder()
                    .name("example")  // Изменено
                    .build();

            company = companyRepository.save(company);


            User user = User.builder()
                    .firstName("Tamer")  // Используем правильное название поля
                    .lastName("Bilici")  // Используем правильное название поля
                    .email("admin@example.com")
                    .password(new BCryptPasswordEncoder().encode("admin"))
                    .role(ROLE_ADMIN)
                    .companyName(company.getName())  // Изменено
                    .company(company)  // Проверьте также это название
                    .build();

            user = userRepository.save(user);


        } else {
            log.warn("Пользователь с правами администратора существует");
        }
    }
}

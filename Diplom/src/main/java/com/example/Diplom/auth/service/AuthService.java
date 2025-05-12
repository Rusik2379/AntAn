package com.example.Diplom.auth.service;

import com.example.Diplom.auth.request.LoginRequest;
import com.example.Diplom.auth.request.SignUpRequest;
import com.example.Diplom.auth.response.AuthResponse;
import com.example.Diplom.models.Company;
import com.example.Diplom.models.User;
import com.example.Diplom.repositories.CompanyRepository;
import com.example.Diplom.repositories.UserRepository;
import com.example.Diplom.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.example.Diplom.models.enums.Role.ROLE_DIRECTOR;
import static com.example.Diplom.models.enums.Role.ROLE_USER;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse register(SignUpRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            return new AuthResponse("User with this email already exists", HttpStatus.BAD_REQUEST);
        }

        Company company = null;
        if (registerRequest.getCompanyname() != null && !registerRequest.getCompanyname().trim().isEmpty()) {
            company = companyRepository.findByName(registerRequest.getCompanyname().trim())
                    .orElse(new Company(registerRequest.getCompanyname().trim()));
            company = companyRepository.save(company);
        }

        User user = User.builder()
                .firstName(registerRequest.getFirstname())
                .lastName(registerRequest.getLastname())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .companyName(registerRequest.getCompanyname() != null ? registerRequest.getCompanyname().trim() : null)
                .company(company)
                .phone(registerRequest.getPhone() != null ? registerRequest.getPhone() : "")
                .address(registerRequest.getAddress() != null ? registerRequest.getAddress() : "")
                .role(registerRequest.getCompanyname() == null || registerRequest.getCompanyname().trim().isEmpty()
                        ? ROLE_USER : ROLE_DIRECTOR)
                .build();

        userRepository.save(user);

        return AuthResponse.builder()
                .message("User registered successfully")
                .response(HttpStatus.OK)
                .email(user.getEmail())
                .role(user.getRole().name())
                .companyname(user.getCompanyName())
                .id(user.getId())
                .companyid(company != null ? company.getId() : null)
                .build();
    }

    public AuthResponse login(LoginRequest loginRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String token = jwtService.generateToken(
                user,
                user.getId(),
                user.getCompany() != null ? user.getCompany().getId() : null
        );

        String message = switch (user.getRole()) {
            case ROLE_ADMIN -> "Admin logged in successfully";
            case ROLE_DIRECTOR -> "Director logged in successfully";
            default -> "User logged in successfully";
        };

        return AuthResponse.builder()
                .message(message)
                .response(HttpStatus.OK)
                .token(token)
                .email(user.getEmail())
                .role(user.getRole().name())
                .companyname(user.getCompanyName())
                .id(user.getId())
                .companyid(user.getCompany() != null ? user.getCompany().getId() : null)
                .build();
    }
}
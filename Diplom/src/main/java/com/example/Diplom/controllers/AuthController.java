package com.example.Diplom.controllers;

import com.example.Diplom.auth.request.LoginRequest;
import com.example.Diplom.auth.request.SignUpRequest;
import com.example.Diplom.auth.response.AuthResponse;
import com.example.Diplom.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@CrossOrigin(origins = "*")

public class AuthController {

    private final AuthService authenticationService;

    @CrossOrigin(origins = "http://localhost:3000")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/registration")
    public ResponseEntity<AuthResponse> signUp(@Valid @RequestBody SignUpRequest signUpRequest) {
        return ResponseEntity.ok(authenticationService.register(signUpRequest));
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authenticationService.login(loginRequest));
    }
}
package com.example.Diplom.auth.response;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@Builder
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private String message;
    private HttpStatus response;
    private String email;
    private String role;
    private String companyname;
    private Long id;
    private Long companyid;

    public AuthResponse(String token, String message, HttpStatus response, String email, String role, String companyname, Long id, Long companyid) {
        this.token = token;
        this.message = message;
        this.response = response;
        this.email = email;
        this.role = role;
        this.companyname = companyname;
        this.id = id;
        this.companyid = companyid;
    }

    public AuthResponse(String message, HttpStatus response) {
        this.message = message;
        this.response = response;
    }
}
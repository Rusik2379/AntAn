package com.example.Diplom.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Добавляем CORS заголовки
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "authorization, content-type");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        if ("OPTIONS".equals(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        try {
            final String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            final String jwt = cleanToken(authHeader.substring(7));

            if (jwt.isEmpty()) {
                log.warn("Empty JWT token");
                sendUnauthorizedError(response, "Invalid JWT token");
                return;
            }

            if (!isValidJwtStructure(jwt)) {
                log.warn("Invalid JWT structure: {}", jwt);
                sendUnauthorizedError(response, "Invalid JWT structure");
                return;
            }

            final String userEmail = jwtService.extractUsername(jwt);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    setAuthenticationInContext(request, userDetails);
                } else {
                    log.warn("Invalid JWT token for user: {}", userEmail);
                    sendUnauthorizedError(response, "Invalid JWT token");
                    return;
                }
            }

            filterChain.doFilter(request, response);

        } catch (UsernameNotFoundException e) {
            log.error("User not found: {}", e.getMessage());
            sendUnauthorizedError(response, "User not found");
        } catch (Exception e) {
            log.error("Authentication error: {}", e.getMessage(), e);
            sendUnauthorizedError(response, "Authentication failed");
        }
    }

    private String cleanToken(String token) {
        if (token == null) return "";
        return token.replaceAll("\\s+", "");
    }

    private boolean isValidJwtStructure(String jwt) {
        int dotCount = 0;
        for (char c : jwt.toCharArray()) {
            if (c == '.') dotCount++;
        }
        return dotCount == 2;
    }

    private void setAuthenticationInContext(HttpServletRequest request, UserDetails userDetails) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    private void sendUnauthorizedError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }
}
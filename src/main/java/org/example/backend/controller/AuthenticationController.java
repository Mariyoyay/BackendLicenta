package org.example.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.backend.DTO.RegisterRequestDTO;
import org.example.backend.model.User;
import org.example.backend.service.AuthenticationService;
import org.example.backend.service.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService, JwtService jwtService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDTO registerRequestDTO) {
        User registeredUser = authenticationService.register(registerRequestDTO);
        return ResponseEntity.ok(registeredUser);
    }

    @PostMapping("/refresh")
    public void refresh(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String authorization = request.getHeader(AUTHORIZATION);
        Map<String, String> tokens = authenticationService.refresh(authorization);

        if (tokens.containsKey("error_message")){
            String error_message = tokens.get("error_message");
            response.setHeader("error", error_message);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(APPLICATION_JSON_VALUE);
            new ObjectMapper().writeValue(response.getOutputStream(), tokens);
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(APPLICATION_JSON_VALUE);
        new ObjectMapper().writeValue(response.getOutputStream(), tokens);
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String authorization = request.getHeader(AUTHORIZATION);
        Map<String, Object> reply = authenticationService.logout(authorization);

        if (reply.containsKey("error_message")){
            String error_message = (String) reply.get("error_message");
            response.setHeader("error", error_message);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(APPLICATION_JSON_VALUE);
            new ObjectMapper().writeValue(response.getOutputStream(), reply);
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(APPLICATION_JSON_VALUE);
        new ObjectMapper().writeValue(response.getOutputStream(), reply);
    }
}

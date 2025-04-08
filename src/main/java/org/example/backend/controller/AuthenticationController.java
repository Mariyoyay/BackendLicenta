package org.example.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.backend.DTO.LoginRequestDTO;
import org.example.backend.DTO.LoginResponseDTO;
import org.example.backend.DTO.LogoutResponseDTO;
import org.example.backend.DTO.RegisterRequestDTO;
import org.example.backend.model.User;
import org.example.backend.service.AuthenticationService;
import org.example.backend.service.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final JwtService jwtService;

    public AuthenticationController(AuthenticationService authenticationService, JwtService jwtService) {
        this.authenticationService = authenticationService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDTO registerRequestDTO) {
        User registeredUser = authenticationService.register(registerRequestDTO);
        return ResponseEntity.ok(registeredUser);
    }

//    @PostMapping("/login")
//    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginRequestDTO) {
//        User logedinUser = authenticationService.login(loginRequestDTO);
//
//        String token = jwtService.generateToken(logedinUser);
//
//        LoginResponseDTO loginResponseDTO = new LoginResponseDTO();
//        loginResponseDTO.setToken(token);
//        loginResponseDTO.setExpiresIn(jwtService.getExpirationTime());
//
//        return ResponseEntity.ok(loginResponseDTO);
//    }
//
//    @PostMapping("/logout")
//    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
//        System.out.println("aci?");
//        LogoutResponseDTO logoutResponseDTO = new LogoutResponseDTO();
//        logoutResponseDTO.setSuccess(authenticationService.logout(request, response));
//
//        return ResponseEntity.ok(logoutResponseDTO);
//    }
}

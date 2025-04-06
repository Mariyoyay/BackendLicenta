package org.example.backend.Controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.example.backend.DTO.LoginRequestDTO;
import org.example.backend.DTO.LoginResponseDTO;
import org.example.backend.DTO.RegisterRequestDTO;
import org.example.backend.Model.User;
import org.example.backend.Repository.RoleRepository;
import org.example.backend.Repository.UserRepository;
import org.example.backend.Service.AuthenticationService;
import org.example.backend.Service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

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

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginRequestDTO) {
        User logedinUser = authenticationService.login(loginRequestDTO);

        String token = jwtService.generateToken(logedinUser);

        LoginResponseDTO loginResponseDTO = new LoginResponseDTO();
        loginResponseDTO.setToken(token);
        loginResponseDTO.setExpiresIn(jwtService.getExpirationTime());

        return ResponseEntity.ok(loginResponseDTO);
    }
}

package org.example.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.backend.DTO.RegisterRequestDTO;
import org.example.backend.model.User;
import org.example.backend.service.AuthenticationService;
import org.example.backend.service.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final JwtService jwtService;

    public AuthenticationController(AuthenticationService authenticationService, JwtService jwtService, JwtService jwtService1) {
        this.authenticationService = authenticationService;
        this.jwtService = jwtService1;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDTO registerRequestDTO) {
        User registeredUser = authenticationService.register(registerRequestDTO);
        return ResponseEntity.ok(registeredUser);
    }

    @PostMapping("/refresh")
    public void refresh(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String refresh_token_received = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("refresh_token")) {
                    refresh_token_received = cookie.getValue();
                    break;
                }
            }
        }

        Map<String, String> tokens = authenticationService.refresh(refresh_token_received);

        if (tokens.containsKey("error_message")){
            String error_message = tokens.get("error_message");
            response.setHeader("error", error_message);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(APPLICATION_JSON_VALUE);
            new ObjectMapper().writeValue(response.getOutputStream(), tokens);
            return;
        }

        String refresh_token_provided = tokens.remove("refresh_token");
        Date expiry_date = jwtService.extractClaim(refresh_token_provided, Claims::getExpiration);

        Cookie refreshTokenCookie = new Cookie("refresh_token", refresh_token_provided);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setMaxAge((int) ((expiry_date.getTime() - System.currentTimeMillis())/1000)); //Time in seconds
//        refreshTokenCookie.setSecure(true);
        response.addCookie(refreshTokenCookie);

        System.out.println("-------------------------------------------------------REFRESHED");

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(APPLICATION_JSON_VALUE);
        new ObjectMapper().writeValue(response.getOutputStream(), tokens);
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String refresh_token = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("refresh_token")) {
                    refresh_token = cookie.getValue();
                    break;
                }
            }
        }

        Map<String, Object> reply = authenticationService.logout(refresh_token);

        if (reply.containsKey("error_message")){
            String error_message = (String) reply.get("error_message");
            response.setHeader("error", error_message);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(APPLICATION_JSON_VALUE);
            new ObjectMapper().writeValue(response.getOutputStream(), reply);
            return;
        }

        Cookie refreshTokenCookie = new Cookie("refresh_token", null);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setMaxAge(0); //Time in seconds
//        refreshTokenCookie.setSecure(true);
        response.addCookie(refreshTokenCookie);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(APPLICATION_JSON_VALUE);
        new ObjectMapper().writeValue(response.getOutputStream(), reply);
    }
}

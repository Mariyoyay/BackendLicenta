package org.example.backend.service;

import io.jsonwebtoken.Claims;
import org.example.backend.DTO.RegisterRequestDTO;
import org.example.backend.model.RefreshToken;
import org.example.backend.model.Role;
import org.example.backend.model.User;
import org.example.backend.repository.RefreshTokenRepository;
import org.example.backend.repository.RoleRepository;
import org.example.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static org.example.backend.utils.RoleNames.ROLE_PATIENT;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthenticationService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder, JwtService jwtService, RefreshTokenRepository refreshTokenRepository
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public User register(RegisterRequestDTO registerRequestDTO) {
        User user = new User();
        user.setEmail(registerRequestDTO.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequestDTO.getPassword()));
        user.setFirstName(registerRequestDTO.getFirstName());
        user.setLastName(registerRequestDTO.getLastName());
        user.setDateOfBirth(registerRequestDTO.getDateOfBirth());
        user.setPhone(registerRequestDTO.getPhone());
        user.setRoles(Set.of(roleRepository.findByName(ROLE_PATIENT).get()));
        return userRepository.save(user);
    }

    @Transactional
    public Map<String, String> refresh(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")){
            throw new RuntimeException("Refresh token is missing");
        }

        final String refresh_token = authorizationHeader.substring(7);

        // checking if blacklisted
        Optional<RefreshToken> token = refreshTokenRepository.findByRefreshToken(refresh_token);
        if (!token.isPresent()) {
            throw new RuntimeException("Refresh token not found");
        }
        RefreshToken refreshToken = token.get();
        if (refreshToken.getIsBlacklisted()){
            throw new RuntimeException("Refresh token is blacklisted");
        }

        try{
            final Claims decodedToken = jwtService.extractAllClaims(refresh_token);
            final String username = decodedToken.getSubject();

            Optional<User> userOptional = userRepository.findByEmail(username);
            if (userOptional.isEmpty()) throw new RuntimeException("User not found");
            User user = userOptional.get();

            Map<String, Object> claims = new HashMap<>();
            claims.put("roles", user.getRoles().stream().map(Role::getName).collect(Collectors.toList()));
            String access_token = jwtService.generateToken(claims, user);

            Map<String, String> tokens = new HashMap<>();
            tokens.put("access_token", access_token);
            tokens.put("refresh_token", refresh_token);

            return tokens;
        } catch (Exception e){
            e.printStackTrace();

            Map<String, String> error = new HashMap<>();
            error.put("error_message", e.getMessage());

            return error;
        }
    }

    @Transactional
    public Map<String, Object> logout(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")){
            throw new RuntimeException("Refresh token is missing");
        }

        final String refresh_token = authorizationHeader.substring(7);

        // checking if already blacklisted
        Optional<RefreshToken> token = refreshTokenRepository.findByRefreshToken(refresh_token);
        if (!token.isPresent()) {
            throw new RuntimeException("Refresh token not saved in repository");
        }

        RefreshToken refreshToken = token.get();
        if (refreshToken.getIsBlacklisted()){
            throw new RuntimeException("Refresh token is already blacklisted");
        }


        Map<String, Object> reply = new HashMap<>();


        try {
            if (jwtService.isTokenExpired(refresh_token)) {
                reply.put("success", true);
                reply.put("logout", "Token was already expired");
                return reply;
            }
            Optional<User> userOptional = userRepository.findByEmail(jwtService.extractUsername(refresh_token));
            if (userOptional.isEmpty()) throw new RuntimeException("User not found");
            refreshTokenRepository.invalidateAllByUser(userOptional.get());

            reply.put("success", true);
            reply.put("logout", "Token successfully revoked");
            return reply;
        } catch (Exception e){
            e.printStackTrace();

            Map<String, Object> error = new HashMap<>();
            error.put("error_message", e.getMessage());

            return error;
        }
    }
}

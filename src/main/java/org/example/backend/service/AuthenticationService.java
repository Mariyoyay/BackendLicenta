package org.example.backend.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.backend.DTO.LoginRequestDTO;
import org.example.backend.DTO.RegisterRequestDTO;
import org.example.backend.model.User;
import org.example.backend.repository.RoleRepository;
import org.example.backend.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(RegisterRequestDTO registerRequestDTO) {
        User user = new User();
        user.setEmail(registerRequestDTO.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequestDTO.getPassword()));
        user.setFirstName(registerRequestDTO.getFirstName());
        user.setLastName(registerRequestDTO.getLastName());
        user.setDateOfBirth(registerRequestDTO.getDateOfBirth());
        user.setPhone(registerRequestDTO.getPhone());
        user.setRoles(Set.of(roleRepository.findByName("ROLE_PATIENT").get()));
        return userRepository.save(user);
    }

    public User login(LoginRequestDTO loginRequestDTO) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDTO.getEmail(),
                        loginRequestDTO.getPassword()
                )
        );

        return userRepository.findByEmail(loginRequestDTO.getEmail()).orElseThrow();
    }

////    @Transactional for when we put refresh tokens
//    public Boolean logout(HttpServletRequest request, HttpServletResponse response){
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//
//        if(auth != null && auth.isAuthenticated()){
//            new SecurityContextLogoutHandler().logout(request, response, auth);
//
////            String token = request.getHeader("Authorization");
////            if(token != null && token.startsWith("Bearer ")){
////                token = token.substring(7);
////            } else {
////                throw new RuntimeException("No refresh token");
////            }
////
////            RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
////                    .orElseThrow(() -> new RuntimeException("REFRESH_TOKEN_DOES_NOT_EXIST"));
////
////            refreshToken.setRefreshTokenStatus(RefreshTokenStatus.REVOKED);
////
////            refreshTokenRepository.save(refreshToken);
//
//            return true;
//        } else {
//            throw new RuntimeException("User not found");
//        }
//    }
}

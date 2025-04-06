package org.example.backend.Service;

import org.example.backend.DTO.LoginRequestDTO;
import org.example.backend.DTO.RegisterRequestDTO;
import org.example.backend.Model.User;
import org.example.backend.Repository.RoleRepository;
import org.example.backend.Repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
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
}

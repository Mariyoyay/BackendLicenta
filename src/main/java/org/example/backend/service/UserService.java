package org.example.backend.service;

import org.example.backend.model.User;
import org.example.backend.repository.RoleRepository;
import org.example.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static org.example.backend.utils.RoleNames.ROLE_PATIENT;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public User addRolesToUser(String email, List<String> roles) {
        User user;
        if (userRepository.existsByEmail(email)) {
            user = userRepository.findByEmail(email).get();
        } else throw new RuntimeException("Invalid email. User doesn't exist");

        roles.forEach(role -> user.getRoles().add(roleRepository.findByName(role).get()));
        return userRepository.save(user);
    }

    @Transactional
    public User deleteRolesFromUser(String email, List<String> roles) {
        User user;
        if (userRepository.existsByEmail(email)) {
            user = userRepository.findByEmail(email).get();
        } else throw new RuntimeException("Invalid email. User doesn't exist");

        user.setRoles(user.getRoles().stream().filter(role ->
            role.getName().equals(ROLE_PATIENT) || !roles.contains(role.getName())
        ).collect(Collectors.toSet()));

        return userRepository.save(user);
    }
}

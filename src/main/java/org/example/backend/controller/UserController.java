package org.example.backend.controller;

import org.example.backend.DTO.RegisterRequestDTO;
import org.example.backend.model.User;
import org.example.backend.service.AuthenticationService;
import org.example.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final AuthenticationService authenticationService;

    public UserController(UserService userService, AuthenticationService authenticationService) {
        this.userService = userService;
        this.authenticationService = authenticationService;
    }

    @GetMapping("/get/all")
    public ResponseEntity<List<User>> getUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("get/by_role/{role}")
    public ResponseEntity<List<User>> getUsersByRole(@PathVariable("role") String role) {
        return ResponseEntity.ok(userService.getUsersByRole(role));
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<User> getUser(@PathVariable("username") String username) {
        User user = userService.getUserByEmail(username);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/myself")
    public ResponseEntity<?> getMyself() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            String userEmail = (String) authentication.getPrincipal();

            User user = userService.getUserByEmail(userEmail);

            return ResponseEntity.ok(user);
        }

        return ResponseEntity.badRequest().body("No authentication found");
    }

    @PostMapping("/doctor/set_color")
    public ResponseEntity<?> setDoctorColor(@RequestBody String color) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            String doctorEmail = (String) authentication.getPrincipal();

            User doctor = userService.setDoctorColor(doctorEmail, color);

            return ResponseEntity.ok(doctor);
        }

        return ResponseEntity.badRequest().body("No authentication found");
    }

    @GetMapping("/public_resource")
    public String getPublicResource() {return "You are accessing the Public Resource";}


    @PostMapping("manage/add")
    public ResponseEntity<User> addUser(@RequestBody RegisterRequestDTO newUser) {
        User addedUser = authenticationService.registerByEmployee(newUser);
        return ResponseEntity.ok(addedUser);
    }

    // ADMIN SPECIFIC ACTIONS

    @PostMapping("/roles/manage/{username}/add")
    public ResponseEntity<?> addRolesToUser(@PathVariable("username") String email, @RequestBody List<String> roles) {
        if (roles.isEmpty()) {
            return ResponseEntity.ok("No roles needed to be added");
        }

        User savedUser = userService.addRolesToUser(email, roles);

        return ResponseEntity.ok(savedUser);
    }

    @DeleteMapping("/roles/manage/{username}/remove")
    public ResponseEntity<?> removeRolesFromUser(@PathVariable("username") String email, @RequestBody List<String> roles) {
        if (roles.isEmpty()) {
            return ResponseEntity.ok("No roles needed to be deleted");
        }

        User savedUser = userService.deleteRolesFromUser(email, roles);

        return ResponseEntity.ok(savedUser);
    }
}

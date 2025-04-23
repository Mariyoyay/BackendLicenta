package org.example.backend.controller;

import org.example.backend.DTO.RegisterRequestDTO;
import org.example.backend.model.User;
import org.example.backend.service.AuthenticationService;
import org.example.backend.service.UserService;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/getUsers")
    public ResponseEntity<List<User>> getUsers() {
        return ResponseEntity.ok(userService.findAll());
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

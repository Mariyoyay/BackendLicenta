package org.example.backend.controller;

import org.example.backend.model.User;
import org.example.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/getUsers")
    public ResponseEntity<List<User>> getUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/public_resource")
    public String getPublicResource() {return "You are accessing the Public Resource";}


    // ADMIN SPECIFIC ACTIONS

    @PostMapping("/{username}/addRoles")
    public ResponseEntity<?> addRolesToUser(@PathVariable("username") String email, @RequestBody List<String> roles) {
        if (roles.isEmpty()) {
            return ResponseEntity.ok("No roles needed to be added");
        }

        User savedUser = userService.addRolesToUser(email, roles);

        return ResponseEntity.ok(savedUser);
    }

    @DeleteMapping("/{username}/removeRoles")
    public ResponseEntity<?> removeRolesFromUser(@PathVariable("username") String email, @RequestBody List<String> roles) {
        if (roles.isEmpty()) {
            return ResponseEntity.ok("No roles needed to be deleted");
        }

        User savedUser = userService.deleteRolesFromUser(email, roles);

        return ResponseEntity.ok(savedUser);
    }
}

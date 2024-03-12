package com.example.light.controller;

import com.example.light.entity.User;
import com.example.light.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Secured("ADMIN")
    @GetMapping("/")
    public ResponseEntity<?> getUsers() {
        try {
            List<User> users = userService.getAllUsers();
            log.info("Retrieved {} users", users.size());
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Failed to retrieve users: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve users: " + e.getMessage());
        }
    }

    @Secured("ADMIN")
    @PostMapping("/{id}/role")
    public ResponseEntity<?> assignOperatorRole(@PathVariable("id") Long userId) {
        try {
            userService.assignOperatorRole(userId);
            log.info("Operator role assigned to user with id {}", userId);
            return ResponseEntity.ok("Operator role assigned successfully.");
        } catch (Exception e) {
            log.error("Failed to assign operator role to user with id {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to assign operator role: " + e.getMessage());
        }
    }
}

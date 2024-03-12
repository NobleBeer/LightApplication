package com.example.light.service;

import com.example.light.entity.Role;
import com.example.light.entity.User;
import com.example.light.entity.role.ERole;
import com.example.light.repository.RoleRepository;
import com.example.light.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Transactional
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public void assignOperatorRole(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        Role operatorRole = roleRepository.findByName(ERole.OPERATOR);
        if (operatorRole == null) {
            throw new IllegalArgumentException("Role OPERATOR not found in the database.");
        }

        if (!user.getRoles().contains(operatorRole)) {
            user.getRoles().add(operatorRole);
            userRepository.save(user);
        } else {
            throw new IllegalArgumentException("User already has role OPERATOR.");
        }
    }
}

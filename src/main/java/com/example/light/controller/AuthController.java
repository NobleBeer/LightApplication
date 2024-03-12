package com.example.light.controller;

import com.example.light.config.CustomUserDetails;
import com.example.light.config.JwtService;
import com.example.light.config.UserDetailsServiceImpl;
import com.example.light.dto.AuthRequestDTO;
import com.example.light.dto.JwtResponseDTO;
import com.example.light.entity.Role;
import com.example.light.entity.User;
import com.example.light.entity.role.ERole;
import com.example.light.repository.RoleRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private final JwtService jwtService;
    final AuthenticationManager authenticationManager;
    final PasswordEncoder passwordEncoder;
    private final UserDetailsServiceImpl userDetailsService;
    private final RoleRepository roleRepository;

    @Autowired
    public AuthController(JwtService jwtService,
                          AuthenticationManager authenticationManager,
                          UserDetailsServiceImpl userDetailsService,
                          PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    //TODO: Это костыль
    @PostMapping("/signin")
    public ResponseEntity<?> createUser(@Valid @RequestBody AuthRequestDTO authRequestDTO) {
        log.debug("password {}", authRequestDTO.getPassword());
        User user = new User();
        user.setName(authRequestDTO.getUsername());
        user.setPassword(passwordEncoder.encode(authRequestDTO.getPassword()));

        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName(ERole.USER);
        roles.add(userRole);
        user.setRoles(roles);

        userDetailsService.createUser(user);

        return ResponseEntity.ok("User was created");
    }

    @PostMapping("/login")
    public JwtResponseDTO authenticateAndGetToken(@RequestBody AuthRequestDTO authRequestDTO) {
        CustomUserDetails userDetails = userDetailsService.loadUserByUsername(authRequestDTO.getUsername());
        log.debug("username {}", userDetails.getUsername());
        if (passwordEncoder.matches(authRequestDTO.getPassword(), userDetails.getPassword())) {

            return JwtResponseDTO.builder()
                    .accessToken(jwtService.generateToken(authRequestDTO.getUsername())).build();
        } else {
            throw new BadCredentialsException("Invalid username or password");
        }
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            request.getSession().invalidate();
        }

        return ResponseEntity.ok("Logout");
    }

}

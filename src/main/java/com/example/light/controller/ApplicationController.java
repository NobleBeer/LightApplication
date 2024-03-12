package com.example.light.controller;

import com.example.light.config.UserDetailsServiceImpl;
import com.example.light.dto.ApplicationDTO;
import com.example.light.entity.Application;
import com.example.light.entity.Role;
import com.example.light.entity.User;
import com.example.light.entity.role.EAppStatus;
import com.example.light.entity.role.ERole;
import com.example.light.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/app")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ApplicationController {

    private final ApplicationService applicationService;
    private final UserDetailsServiceImpl userDetailsService;

    @Autowired
    public ApplicationController(ApplicationService applicationService, UserDetailsServiceImpl userDetailsService) {
        this.applicationService = applicationService;
        this.userDetailsService = userDetailsService;
    }

    @Secured("USER")
    @PostMapping("/")
    public ResponseEntity<?> createApp(@Valid @RequestBody ApplicationDTO applicationDTO) {
        User user = userDetailsService.getUser();
        applicationService.createAndSaveApplication(user, applicationDTO);

        log.info("Application created by user: {}", user.getName());
        return ResponseEntity.ok("Application was created");
    }

    @Secured("USER")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateApp(@PathVariable Long id, @Valid @RequestBody ApplicationDTO applicationDTO) {
        User user = userDetailsService.getUser();
        applicationService.updateApplication(user, id, applicationDTO);

        log.info("Application updated by user: {}", user.getName());
        return ResponseEntity.ok("Application was updated");
    }

    @Secured("OPERATOR")
    @PostMapping("/{id}/{status}")
    public ResponseEntity<?> processApplication(@PathVariable("id") Long applicationId,
                                                @PathVariable("status") String status) {
        try {
            EAppStatus appStatus = EAppStatus.valueOf(status.toUpperCase());
            applicationService.processApplication(applicationId, appStatus);

            log.info("Application {} processed successfully by operator", applicationId);
            return ResponseEntity.ok("Application processed successfully");
        } catch (Exception e) {
            log.error("Failed to process application {}: {}", applicationId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process application: " + e.getMessage());
        }
    }

    @Secured({"USER", "OPERATOR", "ADMIN"})
    @GetMapping("/")
    public ResponseEntity<?> getApplications(@RequestParam(value = "status", required = false) String status,
                                             @RequestParam(value = "username", required = false) String username,
                                             @RequestParam(value = "offset") int offset,
                                             @RequestParam(value = "limit", defaultValue = "5") int limit,
                                             @RequestParam(value = "sortBy", defaultValue = "date") String sortBy,
                                             @RequestParam(value = "ascending", defaultValue = "true") boolean ascending) {
        try {
            User user = userDetailsService.getUser();
            Set<Role> roles = user.getRoles();

            // Логирование параметров запроса
            log.info("Request parameters: status={}, username={}, offset={}, limit={}, sortBy={}, ascending={}",
                    status, username, offset, limit, sortBy, ascending);

            if (status != null && username != null) {
                log.warn("Both status and username parameters are provided, which is not allowed");
                return ResponseEntity.badRequest()
                        .body("Parameters 'status' and 'username' cannot be specified simultaneously.");
            }

            Specification<Application> specification = Specification.where(null);

            if (roles.stream().anyMatch(role -> role.getName().equals(ERole.ADMIN) ||
                    role.getName().equals(ERole.OPERATOR))) {
                if (username != null && !username.isEmpty()) {
                    specification = specification.and(applicationService.hasSearchQueryUser(username));
                }
            }

            if (roles.stream().anyMatch(role -> role.getName().equals(ERole.OPERATOR))) {
                if (status != null && !status.isEmpty()) {
                    specification = specification.and(applicationService.hasStatus(ERole.OPERATOR.toString()));
                }
            }

            if (roles.stream().anyMatch(role -> role.getName().equals(ERole.USER))) {
                if (status != null && !status.isEmpty()) {
                    specification = specification.and(applicationService.hasStatus(status));
                }
            }

            List<Application> applications = applicationService.getAllApplications(specification);
            if (sortBy != null && !sortBy.isEmpty()) {
                applications = applicationService.sortApplications(applications, sortBy, ascending);
            }

            int totalCount = applications.size();
            Map<String, Object> response = new HashMap<>();
            response.put("count", totalCount);

            if (offset >= totalCount) {
                response.put("data", new ArrayList<>());
            } else {
                int startIndex = offset;
                int endIndex = Math.min(offset + limit, totalCount);
                List<Application> pagedApplications = applications.subList(startIndex, endIndex);

                response.put("data", pagedApplications);
            }

            log.info("Retrieved {} applications", totalCount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to retrieve applications: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while retrieving applications: " + e.getMessage());
        }
    }
}
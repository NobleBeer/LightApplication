package com.example.light.controller;

import com.example.light.config.UserDetailsServiceImpl;
import com.example.light.dto.ApplicationDTO;
import com.example.light.entity.Application;
import com.example.light.entity.Role;
import com.example.light.entity.User;
import com.example.light.entity.role.ERole;
import com.example.light.service.ApplicationService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
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

        return ResponseEntity.ok("Application was created");
    }

    // А вообще можно было бы и PATCH
    @Secured("USER")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateApp(@PathVariable Long id, @Valid @RequestBody ApplicationDTO applicationDTO) {
        User user = userDetailsService.getUser();
        applicationService.updateApplication(user, id, applicationDTO);
        return ResponseEntity.ok("Application was updated");
    }

    @Secured({"USER", "OPERATOR", "ADMIN"})
    @GetMapping("/")
    public ResponseEntity<?> updateApp(@RequestParam(value = "status", required = false) String status,
                                       @RequestParam(value = "username", required = false) String username,
                                       @RequestParam(value = "offset") int offset,
                                       @RequestParam(value = "limit", defaultValue = "5") int limit,
                                       @RequestParam(value = "sortBy", defaultValue = "date") String sortBy,
                                       @RequestParam(value = "ascending", defaultValue = "true") boolean ascending,
                                       HttpSession session) {
        try {
            User user = userDetailsService.getUser();
            Set<Role> roles = user.getRoles();

            if (status != null && username != null) {
                return ResponseEntity.badRequest()
                        .body("Параметры 'status' и 'username' не могут быть указаны одновременно.");
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

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Произошла ошибка при обновлении приложения: " + e.getMessage());
        }
    }
}

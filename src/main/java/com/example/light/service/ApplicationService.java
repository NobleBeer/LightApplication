package com.example.light.service;

import com.example.light.dto.ApplicationDTO;
import com.example.light.entity.Application;
import com.example.light.entity.User;
import com.example.light.entity.role.EAppStatus;
import com.example.light.repository.ApplicationRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class ApplicationService {
    private final ApplicationRepository applicationRepository;

    @Autowired
    public ApplicationService(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    public void createAndSaveApplication(User user, ApplicationDTO applicationDTO) {
        if (applicationDTO.getAppStatus() != EAppStatus.DRAFT &&
                applicationDTO.getAppStatus() != EAppStatus.SENT) {
            throw new IllegalStateException("Status is rejected");
        }

        Application sentApplication = Application.builder().name(applicationDTO.getName())
                .phoneNumber(applicationDTO.getPhoneNumber())
                .status(applicationDTO.getAppStatus())
                .text(applicationDTO.getText())
                .user(user).build();

        applicationRepository.save(sentApplication);
    }

    public void updateApplication(User user, Long id, ApplicationDTO updatedApplication) {
        Application existingApplication = applicationRepository.findById(id).orElse(null);

        if (user.getId().equals(id) && existingApplication != null
                && existingApplication.getStatus() == EAppStatus.DRAFT) {
            existingApplication.setName(updatedApplication.getName());
            existingApplication.setPhoneNumber(updatedApplication.getPhoneNumber());
            existingApplication.setText(updatedApplication.getText());
            applicationRepository.save(existingApplication);
        } else {
            throw new IllegalStateException("You cannot edit an application");
        }
    }

    public List<Application> getAllApplications(Specification<Application> specification) {
        return applicationRepository.findAll((Sort) specification);
    }

    public Specification<Application> hasStatus(String status) {
        return (root, query, builder) -> builder.equal(root.get("status"), status);
    }

    public Specification<Application> hasSearchQueryUser(String searchQuery) {
        return (root, query, builder) -> builder.like(builder
                .lower(root.get("user").get("name")), "%" + searchQuery.toLowerCase() + "%");
    }

    public List<Application> sortApplications(List<Application> applications,
                                    String sortBy,
                                    boolean ascending) {
        applications.sort(getComparator(sortBy, ascending));
        return applications;
    }

    //TODO: ну можно еще как-нибудь фильтровать
    public Comparator<Application> getComparator(String sortBy, boolean ascending) {
        Comparator<Application> comparator = switch (sortBy.toLowerCase()) {
            case "date" -> Comparator.comparing(Application::getCreationDate,
                    Comparator.nullsLast(Comparator.naturalOrder()));
            default -> throw new IllegalArgumentException("Invalid sorting parameter: " + sortBy);
        };
        if (!ascending) {
            comparator = comparator.reversed();
        }
        return comparator;
    }




}

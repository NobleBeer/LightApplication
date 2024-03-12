package com.example.light;

import com.example.light.config.UserDetailsServiceImpl;
import com.example.light.controller.ApplicationController;
import com.example.light.dto.ApplicationDTO;
import com.example.light.entity.User;
import com.example.light.service.ApplicationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class ApplicationTests {
    @Mock
    private ApplicationService applicationService;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @InjectMocks
    private ApplicationController applicationController;

    @Test
    public void testGetApplications() {
        when(userDetailsService.getUser()).thenReturn(new User());
        when(applicationService.getAllApplications(any())).thenReturn(Collections.emptyList());

        ResponseEntity<?> responseEntity = applicationController
                .getApplications(null, null, 0, 5, "date", true);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isInstanceOf(java.util.Map.class);
        verify(applicationService, times(1)).getAllApplications(any());
    }

    @Test
    public void testCreateApp() {
        when(userDetailsService.getUser()).thenReturn(new User());

        ResponseEntity<?> responseEntity = applicationController.createApp(new ApplicationDTO());

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isEqualTo("Application was created");
        verify(applicationService, times(1))
                .createAndSaveApplication(any(User.class), any(ApplicationDTO.class));
    }
}

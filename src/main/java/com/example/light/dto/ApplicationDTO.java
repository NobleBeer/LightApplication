package com.example.light.dto;

import com.example.light.entity.role.EAppStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationDTO {
    @NotNull(message = "Name cannot be null")
    private String name;
    @NotNull(message = "Phone number cannot be null")
    private String phoneNumber;
    @NotNull(message = "Text cannot be null")
    private String text;
    @NotNull(message = "Status cannot be null")
    private EAppStatus appStatus;
}

package com.example.light.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthRequestDTO {

    @NotNull(message = "Uername cannot be null")
    private String username;
    @NotNull(message = "Password cannot be null")
    private String password;

}
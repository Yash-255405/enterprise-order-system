package com.company.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {

    @NotBlank(message = "Username cannot be blank")
    private String username;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    private String email;
}

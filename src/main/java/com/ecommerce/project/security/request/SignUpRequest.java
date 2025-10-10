package com.ecommerce.project.security.request;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;
@Data
public class SignUpRequest {
    @NotBlank
    @Size(min = 3, max = 20)

    @NotBlank
    private String username;

    @NotBlank
    @Size(min = 6, max = 40)
    private String password;

    @Email
    private String email;
    private Set<String> role;
}

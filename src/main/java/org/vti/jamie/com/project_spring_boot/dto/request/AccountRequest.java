package org.vti.jamie.com.project_spring_boot.dto.request;

import jakarta.validation.constraints.*;

public record AccountRequest(
        @NotBlank @Email @Size(max = 50) String email,
        @NotBlank @Size(max = 50) String username,
        @NotBlank @Size(max = 50) String fullName,
        @NotNull @Min(1) @Max(255) Short departmentId,
        @NotNull @Min(1) @Max(255) Short positionId
) {}

package org.vti.jamie.com.project_spring_boot.dto.request;

import jakarta.validation.constraints.*;

// Email and username are immutable identities; PUT updates profile and relations.
public record AccountUpdateRequest(
        @NotBlank @Size(max = 50) String fullName,
        @NotNull @Min(1) @Max(255) Short departmentId,
        @NotNull @Min(1) @Max(255) Short positionId
) {}

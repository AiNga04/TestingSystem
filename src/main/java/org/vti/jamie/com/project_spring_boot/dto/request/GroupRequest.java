package org.vti.jamie.com.project_spring_boot.dto.request;

import jakarta.validation.constraints.*;

public record GroupRequest(
        @NotBlank @Size(max = 50) String groupName,
        @NotNull @Min(1) @Max(255) Short creatorId
) {}

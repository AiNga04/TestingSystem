package org.vti.jamie.com.project_spring_boot.dto.request;

import jakarta.validation.constraints.*;

public record GroupAccountRequest(@NotNull @Min(1) @Max(255) Short accountId) {}

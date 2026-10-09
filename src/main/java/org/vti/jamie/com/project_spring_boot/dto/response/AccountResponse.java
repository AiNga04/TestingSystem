package org.vti.jamie.com.project_spring_boot.dto.response;

import java.time.LocalDateTime;

public record AccountResponse(Short accountId, String email, String username, String fullName,
                              DepartmentResponse department, PositionResponse position,
                              LocalDateTime createdAt, LocalDateTime deletedAt) {}

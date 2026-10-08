package org.vti.jamie.com.project_spring_boot.dto.response;

import java.time.LocalDateTime;

public record DepartmentResponse(
        Short departmentId,
        String departmentName,
        LocalDateTime deletedAt
) {}

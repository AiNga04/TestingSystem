package org.vti.jamie.com.project_spring_boot.dto.response;

import java.time.LocalDateTime;

public record AccountSummary(Short accountId, String email, String username,
                             String fullName, LocalDateTime deletedAt) {}

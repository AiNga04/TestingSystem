package org.vti.jamie.com.project_spring_boot.dto.response;

import java.time.LocalDateTime;

public record GroupResponse(Short groupId, String groupName, AccountSummary creator,
                            LocalDateTime createdAt, LocalDateTime deletedAt) {}

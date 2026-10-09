package org.vti.jamie.com.project_spring_boot.dto.response;

import java.time.LocalDateTime;

public record GroupAccountResponse(Short groupId, String groupName,
                                   AccountResponse account, LocalDateTime joinedAt) {}

package org.vti.jamie.com.project_spring_boot.dto.response;

import java.time.LocalDateTime;
import org.vti.jamie.com.project_spring_boot.enums.PositionName;

public record PositionResponse(
        Short positionId,
        PositionName positionName,
        LocalDateTime deletedAt
) {}

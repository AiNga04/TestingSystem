package org.vti.jamie.com.project_spring_boot.dto.request;

import jakarta.validation.constraints.NotNull;
import org.vti.jamie.com.project_spring_boot.enums.PositionName;

public record PositionRequest(
        @NotNull(message = "Tên chức vụ không được để trống")
        PositionName positionName
) {}

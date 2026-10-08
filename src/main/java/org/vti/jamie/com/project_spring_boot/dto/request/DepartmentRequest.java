package org.vti.jamie.com.project_spring_boot.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DepartmentRequest(

        @NotBlank(message = "Tên phòng ban không được để trống")
        @Size(
                max = 30,
                message = "Tên phòng ban không được vượt quá 30 ký tự"
        )
        String departmentName

) {}
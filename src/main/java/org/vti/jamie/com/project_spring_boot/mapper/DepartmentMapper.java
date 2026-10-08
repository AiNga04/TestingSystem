package org.vti.jamie.com.project_spring_boot.mapper;

import org.springframework.stereotype.Component;
import org.vti.jamie.com.project_spring_boot.dto.request.DepartmentRequest;
import org.vti.jamie.com.project_spring_boot.dto.response.DepartmentResponse;
import org.vti.jamie.com.project_spring_boot.entity.Department;

@Component
public class DepartmentMapper {

    public Department toEntity(DepartmentRequest request) {
        return new Department(request.departmentName().trim());
    }

    public DepartmentResponse toResponse(Department entity) {
        return new DepartmentResponse(
                entity.getId(),
                entity.getName()
        );
    }

    public void updateEntity(
            DepartmentRequest request,
            Department entity
    ) {
        entity.setName(
                request.departmentName().trim()
        );
    }
}

package org.vti.jamie.com.project_spring_boot.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.vti.jamie.com.project_spring_boot.dto.request.DepartmentRequest;
import org.vti.jamie.com.project_spring_boot.dto.response.DepartmentResponse;

public interface DepartmentService {

    DepartmentResponse create(DepartmentRequest request);

    DepartmentResponse getById(Short id);

    Page<DepartmentResponse> getAll(
            String keyword,
            boolean deleted,
            Pageable pageable
    );

    DepartmentResponse update(
            Short id,
            DepartmentRequest request
    );

    void delete(Short id);

    DepartmentResponse restore(Short id);
}
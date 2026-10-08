package org.vti.jamie.com.project_spring_boot.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.vti.jamie.com.project_spring_boot.dto.request.PositionRequest;
import org.vti.jamie.com.project_spring_boot.dto.response.PositionResponse;

public interface PositionService {

    PositionResponse create(PositionRequest request);

    PositionResponse getById(Short id);

    Page<PositionResponse> getAll(
            String keyword,
            boolean deleted,
            Pageable pageable
    );

    PositionResponse update(
            Short id,
            PositionRequest request
    );

    void delete(Short id);

    PositionResponse restore(Short id);
}
package org.vti.jamie.com.project_spring_boot.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.vti.jamie.com.project_spring_boot.dto.request.DepartmentRequest;
import org.vti.jamie.com.project_spring_boot.dto.response.DepartmentResponse;
import org.vti.jamie.com.project_spring_boot.entity.Department;
import org.vti.jamie.com.project_spring_boot.exception.DuplicateResourceException;
import org.vti.jamie.com.project_spring_boot.exception.ResourceNotFoundException;
import org.vti.jamie.com.project_spring_boot.exception.ResourceConflictException;
import org.vti.jamie.com.project_spring_boot.mapper.DepartmentMapper;
import org.vti.jamie.com.project_spring_boot.repository.DepartmentRepository;
import org.vti.jamie.com.project_spring_boot.service.DepartmentService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository repository;
    private final DepartmentMapper mapper;

    @Override
    @Transactional
    public DepartmentResponse create(DepartmentRequest request) {

        String name = request.departmentName().trim();

        if (repository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException(
                    "Phòng ban đã tồn tại: " + name
            );
        }

        Department entity = mapper.toEntity(request);

        Department saved = repository.saveAndFlush(entity);

        return mapper.toResponse(saved);
    }

    @Override
    public DepartmentResponse getById(Short id) {

        Department entity = findDepartmentOrThrow(id);

        return mapper.toResponse(entity);
    }

    @Override
    public Page<DepartmentResponse> getAll(
            String keyword,
            Pageable pageable
    ) {

        String normalizedKeyword =
                keyword == null || keyword.isBlank()
                        ? null
                        : keyword.trim();

        return repository
                .search(normalizedKeyword, pageable)
                .map(mapper::toResponse);
    }

    @Override
    @Transactional
    public DepartmentResponse update(
            Short id,
            DepartmentRequest request
    ) {

        Department entity = findDepartmentOrThrow(id);

        String name = request.departmentName().trim();

        boolean duplicate =
                repository.existsByNameIgnoreCaseAndIdNot(
                        name, id
                );

        if (duplicate) {
            throw new DuplicateResourceException(
                    "Phòng ban đã tồn tại: " + name
            );
        }

        mapper.updateEntity(request, entity);

        Department saved = repository.saveAndFlush(entity);

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Short id) {

        Department entity = findDepartmentOrThrow(id);

        if (!entity.getAccounts().isEmpty()) {
            throw new ResourceConflictException(
                    "Không thể xóa phòng ban đang có tài khoản");
        }

        repository.delete(entity);
        repository.flush();
    }

    private Department findDepartmentOrThrow(Short id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy phòng ban với ID: " + id
                ));
    }
}

package org.vti.jamie.com.project_spring_boot.service.impl;

import lombok.RequiredArgsConstructor;
import org.vti.jamie.com.project_spring_boot.enums.PositionName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.vti.jamie.com.project_spring_boot.dto.request.PositionRequest;
import org.vti.jamie.com.project_spring_boot.dto.response.PositionResponse;
import org.vti.jamie.com.project_spring_boot.entity.Position;
import org.vti.jamie.com.project_spring_boot.exception.DuplicateResourceException;
import org.vti.jamie.com.project_spring_boot.exception.ResourceNotFoundException;
import org.vti.jamie.com.project_spring_boot.exception.ResourceConflictException;
import org.vti.jamie.com.project_spring_boot.mapper.PositionMapper;
import org.vti.jamie.com.project_spring_boot.repository.PositionRepository;
import org.vti.jamie.com.project_spring_boot.service.PositionService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PositionServiceImpl implements PositionService {

    private final PositionRepository repository;
    private final PositionMapper mapper;

    @Override
    @Transactional
    public PositionResponse create(PositionRequest request) {

        PositionName name = request.positionName();

        if (repository.existsByName(name)) {
            throw new DuplicateResourceException(
                    "Chức vụ đã tồn tại: " + name
            );
        }

        Position entity = mapper.toEntity(request);

        Position saved = repository.saveAndFlush(entity);

        return mapper.toResponse(saved);
    }

    @Override
    public PositionResponse getById(Short id) {

        Position entity = findPositionOrThrow(id);

        return mapper.toResponse(entity);
    }

    @Override
    public Page<PositionResponse> getAll(
            String keyword,
            boolean deleted,
            Pageable pageable
    ) {

        String normalizedKeyword =
                keyword == null || keyword.isBlank()
                        ? null
                        : keyword.trim();

        return repository
                .search(normalizedKeyword, deleted, pageable)
                .map(mapper::toResponse);
    }

    @Override
    @Transactional
    public PositionResponse update(
            Short id,
            PositionRequest request
    ) {

        Position entity = findActiveForUpdate(id);

        PositionName name = request.positionName();

        boolean duplicate =
                repository.existsByNameAndIdNot(
                        name, id
                );

        if (duplicate) {
            throw new DuplicateResourceException(
                    "Chức vụ đã tồn tại: " + name
            );
        }

        mapper.updateEntity(request, entity);

        Position saved = repository.saveAndFlush(entity);

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Short id) {

        Position entity = findActiveForUpdate(id);

        if (!entity.getAccounts().isEmpty()) {
            throw new ResourceConflictException(
                    "Không thể xóa chức vụ đang có tài khoản");
        }

        entity.softDelete();
        repository.saveAndFlush(entity);
    }

    @Override
    @Transactional
    public PositionResponse restore(Short id) {
        Position entity = repository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chức vụ với ID: " + id));
        if (entity.getDeletedAt() == null) {
            throw new ResourceConflictException("Chức vụ đang hoạt động, không cần khôi phục");
        }
        entity.restore();
        return mapper.toResponse(repository.saveAndFlush(entity));
    }

    private Position findActiveForUpdate(Short id) {
        return repository.findByIdForUpdate(id)
                .filter(entity -> entity.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chức vụ đang hoạt động với ID: " + id));
    }
    private Position findPositionOrThrow(Short id) {
        return repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy chức vụ với ID: " + id
                ));
    }
}

package org.vti.jamie.com.project_spring_boot.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.vti.jamie.com.project_spring_boot.dto.request.GroupRequest;
import org.vti.jamie.com.project_spring_boot.dto.response.GroupResponse;
import org.vti.jamie.com.project_spring_boot.entity.Group;
import org.vti.jamie.com.project_spring_boot.exception.*;
import org.vti.jamie.com.project_spring_boot.mapper.GroupMapper;
import org.vti.jamie.com.project_spring_boot.repository.GroupRepository;
import org.vti.jamie.com.project_spring_boot.service.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupServiceImpl implements GroupService {
    private final GroupRepository repository;
    private final GroupMapper mapper;
    private final ActiveRelations relations;

    @Override @Transactional
    public GroupResponse create(GroupRequest request) {
        var creator = relations.account(request.creatorId());
        String name = request.groupName().trim();
        if (repository.existsByNameIgnoreCase(name)) throw new DuplicateResourceException("Tên nhóm đã tồn tại: " + name);
        return mapper.toResponse(repository.saveAndFlush(new Group(name, creator)));
    }

    @Override
    public GroupResponse getById(Short id) {
        return mapper.toResponse(repository.findByIdAndDeletedAtIsNull(id).orElseThrow(() -> notFound(id)));
    }

    @Override
    public Page<GroupResponse> getAll(String keyword, boolean deleted, Short creatorId, Pageable pageable) {
        String normalized = keyword == null || keyword.isBlank() ? null : keyword.trim();
        return repository.search(normalized, deleted, creatorId, pageable).map(mapper::toResponse);
    }

    @Override @Transactional
    public GroupResponse update(Short id, GroupRequest request) {
        Group entity = activeForUpdate(id);
        var creator = relations.account(request.creatorId());
        String name = request.groupName().trim();
        if (repository.existsByNameIgnoreCaseAndIdNot(name, id)) throw new DuplicateResourceException("Tên nhóm đã tồn tại: " + name);
        entity.setName(name);
        entity.setCreator(creator);
        return mapper.toResponse(repository.saveAndFlush(entity));
    }

    @Override @Transactional
    public void delete(Short id) {
        Group entity = activeForUpdate(id);
        entity.softDelete();
        repository.saveAndFlush(entity);
    }

    @Override @Transactional
    public GroupResponse restore(Short id) {
        Group entity = repository.findByIdForUpdate(id).orElseThrow(() -> notFound(id));
        if (entity.getDeletedAt() == null) throw new ResourceConflictException("Nhóm đang hoạt động");
        if (entity.getCreator() != null) relations.account(entity.getCreator().getId());
        entity.restore();
        return mapper.toResponse(repository.saveAndFlush(entity));
    }

    private Group activeForUpdate(Short id) {
        return repository.findByIdForUpdate(id).filter(g -> g.getDeletedAt() == null).orElseThrow(() -> notFound(id));
    }
    private ResourceNotFoundException notFound(Short id) { return new ResourceNotFoundException("Không tìm thấy nhóm đang hoạt động: " + id); }
}

package org.vti.jamie.com.project_spring_boot.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.vti.jamie.com.project_spring_boot.dto.request.*;
import org.vti.jamie.com.project_spring_boot.dto.response.AccountResponse;
import org.vti.jamie.com.project_spring_boot.entity.*;
import org.vti.jamie.com.project_spring_boot.exception.*;
import org.vti.jamie.com.project_spring_boot.mapper.AccountMapper;
import org.vti.jamie.com.project_spring_boot.repository.AccountRepository;
import org.vti.jamie.com.project_spring_boot.service.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountServiceImpl implements AccountService {
    private final AccountRepository repository;
    private final AccountMapper mapper;
    private final ActiveRelations relations;

    @Override @Transactional
    public AccountResponse create(AccountRequest request) {
        Department department = relations.department(request.departmentId());
        Position position = relations.position(request.positionId());
        String email = request.email().trim();
        String username = request.username().trim();
        if (repository.existsByEmailIgnoreCase(email)) throw new DuplicateResourceException("Email đã tồn tại");
        if (repository.existsByUsernameIgnoreCase(username)) throw new DuplicateResourceException("Username đã tồn tại");
        Account entity = new Account(email, username, request.fullName().trim(), department, position);
        return mapper.toResponse(repository.saveAndFlush(entity));
    }

    @Override
    public AccountResponse getById(Short id) {
        return mapper.toResponse(repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> notFound(id)));
    }

    @Override
    public Page<AccountResponse> getAll(String keyword, boolean deleted, Short departmentId, Short positionId, Pageable pageable) {
        return repository.search(normalize(keyword), deleted, departmentId, positionId, pageable).map(mapper::toResponse);
    }

    @Override @Transactional
    public AccountResponse update(Short id, AccountUpdateRequest request) {
        Account entity = activeForUpdate(id);
        Department department = relations.department(request.departmentId());
        Position position = relations.position(request.positionId());
        entity.changeFullName(request.fullName());
        entity.changeDepartment(department);
        entity.changePosition(position);
        return mapper.toResponse(repository.saveAndFlush(entity));
    }

    @Override @Transactional
    public void delete(Short id) {
        Account entity = activeForUpdate(id);
        entity.softDelete();
        repository.saveAndFlush(entity);
    }

    @Override @Transactional
    public AccountResponse restore(Short id) {
        Account entity = repository.findByIdForUpdate(id).orElseThrow(() -> notFound(id));
        if (entity.getDeletedAt() == null) throw new ResourceConflictException("Tài khoản đang hoạt động");
        relations.department(entity.getDepartment().getId());
        relations.position(entity.getPosition().getId());
        entity.restore();
        return mapper.toResponse(repository.saveAndFlush(entity));
    }

    private Account activeForUpdate(Short id) {
        return repository.findByIdForUpdate(id).filter(a -> a.getDeletedAt() == null).orElseThrow(() -> notFound(id));
    }
    private ResourceNotFoundException notFound(Short id) {
        return new ResourceNotFoundException("Không tìm thấy tài khoản đang hoạt động: " + id);
    }
    private String normalize(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}

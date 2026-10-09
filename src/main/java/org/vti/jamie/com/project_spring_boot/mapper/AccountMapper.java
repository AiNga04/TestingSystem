package org.vti.jamie.com.project_spring_boot.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.vti.jamie.com.project_spring_boot.dto.response.*;
import org.vti.jamie.com.project_spring_boot.entity.Account;

@Component
@RequiredArgsConstructor
public class AccountMapper {
    private final DepartmentMapper departmentMapper;
    private final PositionMapper positionMapper;

    public AccountResponse toResponse(Account entity) {
        return new AccountResponse(entity.getId(), entity.getEmail(), entity.getUsername(), entity.getFullName(),
                departmentMapper.toResponse(entity.getDepartment()), positionMapper.toResponse(entity.getPosition()),
                entity.getCreatedAt(), entity.getDeletedAt());
    }

    public AccountSummary toSummary(Account entity) {
        return entity == null ? null : new AccountSummary(entity.getId(), entity.getEmail(), entity.getUsername(),
                entity.getFullName(), entity.getDeletedAt());
    }
}

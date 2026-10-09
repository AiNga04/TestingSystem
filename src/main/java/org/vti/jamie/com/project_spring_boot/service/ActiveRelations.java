package org.vti.jamie.com.project_spring_boot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.vti.jamie.com.project_spring_boot.entity.*;
import org.vti.jamie.com.project_spring_boot.exception.*;
import org.vti.jamie.com.project_spring_boot.repository.*;

// Call inside a write transaction. Lock referenced rows so validation and writes are atomic.
@Component
@RequiredArgsConstructor
public class ActiveRelations {
    private final DepartmentRepository departments;
    private final PositionRepository positions;
    private final AccountRepository accounts;

    public Department department(Short id) {
        Department entity = departments.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng ban: " + id));
        if (entity.getDeletedAt() != null) throw new ResourceConflictException("Phòng ban đã bị xóa: " + id);
        return entity;
    }

    public Position position(Short id) {
        Position entity = positions.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chức vụ: " + id));
        if (entity.getDeletedAt() != null) throw new ResourceConflictException("Chức vụ đã bị xóa: " + id);
        return entity;
    }

    public Account account(Short id) {
        Account entity = accounts.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản: " + id));
        if (entity.getDeletedAt() != null) throw new ResourceConflictException("Tài khoản đã bị xóa: " + id);
        return entity;
    }
}

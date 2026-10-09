package org.vti.jamie.com.project_spring_boot.service.impl;

import jakarta.persistence.Persistence;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.vti.jamie.com.project_spring_boot.dto.response.GroupAccountResponse;
import org.vti.jamie.com.project_spring_boot.entity.*;
import org.vti.jamie.com.project_spring_boot.entity.Group;
import org.vti.jamie.com.project_spring_boot.exception.*;
import org.vti.jamie.com.project_spring_boot.mapper.GroupAccountMapper;
import org.vti.jamie.com.project_spring_boot.repository.*;
import org.vti.jamie.com.project_spring_boot.service.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupAccountServiceImpl implements GroupAccountService {
    private final GroupAccountRepository repository;
    private final GroupRepository groups;
    private final AccountRepository accounts;
    private final ActiveRelations relations;
    private final GroupAccountMapper mapper;

    @Override @Transactional
    public GroupAccountResponse add(Short groupId, Short accountId) {
        Group group = activeGroupForUpdate(groupId);
        Account account = relations.account(accountId);
        GroupAccountId id = new GroupAccountId(groupId, accountId);
        if (repository.existsById(id)) throw new DuplicateResourceException("Tài khoản đã là thành viên của nhóm");
        GroupAccount membership = repository.saveAndFlush(new GroupAccount(group, account));
        // Keep already-loaded inverse collections consistent without fetching whole member lists.
        if (Persistence.getPersistenceUtil().isLoaded(group, "memberships")) group.getMemberships().add(membership);
        if (Persistence.getPersistenceUtil().isLoaded(account, "groupMemberships")) account.getGroupMemberships().add(membership);
        return mapper.toResponse(membership);
    }

    @Override
    public GroupAccountResponse getById(Short groupId, Short accountId) {
        requireActiveGroup(groupId);
        return mapper.toResponse(repository.findActive(new GroupAccountId(groupId, accountId))
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thành viên trong nhóm")));
    }

    @Override
    public Page<GroupAccountResponse> getAll(Short groupId, String keyword, Pageable pageable) {
        requireActiveGroup(groupId);
        String normalized = keyword == null || keyword.isBlank() ? null : keyword.trim();
        return repository.search(groupId, normalized, pageable).map(mapper::toResponse);
    }

    @Override @Transactional
    public void remove(Short groupId, Short accountId) {
        Group group = activeGroupForUpdate(groupId);
        // Removal also accepts a deleted account, allowing cleanup of an old membership.
        Account account = accounts.findByIdForUpdate(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản: " + accountId));
        GroupAccount membership = repository.findById(new GroupAccountId(groupId, accountId))
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thành viên trong nhóm"));
        if (Persistence.getPersistenceUtil().isLoaded(group, "memberships")) group.getMemberships().remove(membership);
        if (Persistence.getPersistenceUtil().isLoaded(account, "groupMemberships")) account.getGroupMemberships().remove(membership);
        repository.delete(membership);
        repository.flush();
    }

    private void requireActiveGroup(Short id) {
        if (groups.findByIdAndDeletedAtIsNull(id).isEmpty()) throw notFound(id);
    }
    private Group activeGroupForUpdate(Short id) {
        // All membership mutations lock the same parent first, serializing duplicate adds and group deletion.
        return groups.findByIdForUpdate(id).filter(g -> g.getDeletedAt() == null).orElseThrow(() -> notFound(id));
    }
    private ResourceNotFoundException notFound(Short id) { return new ResourceNotFoundException("Không tìm thấy nhóm đang hoạt động: " + id); }
}

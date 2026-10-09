package org.vti.jamie.com.project_spring_boot.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.vti.jamie.com.project_spring_boot.dto.response.GroupResponse;
import org.vti.jamie.com.project_spring_boot.entity.Group;

@Component
@RequiredArgsConstructor
public class GroupMapper {
    private final AccountMapper accountMapper;
    public GroupResponse toResponse(Group entity) {
        return new GroupResponse(entity.getId(), entity.getName(), accountMapper.toSummary(entity.getCreator()),
                entity.getCreatedAt(), entity.getDeletedAt());
    }
}

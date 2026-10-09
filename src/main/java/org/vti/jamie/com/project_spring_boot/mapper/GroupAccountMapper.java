package org.vti.jamie.com.project_spring_boot.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.vti.jamie.com.project_spring_boot.dto.response.GroupAccountResponse;
import org.vti.jamie.com.project_spring_boot.entity.GroupAccount;

@Component
@RequiredArgsConstructor
public class GroupAccountMapper {
    private final AccountMapper accountMapper;
    public GroupAccountResponse toResponse(GroupAccount entity) {
        return new GroupAccountResponse(entity.getId().getGroupId(), entity.getGroup().getName(),
                accountMapper.toResponse(entity.getAccount()), entity.getJoinedAt());
    }
}

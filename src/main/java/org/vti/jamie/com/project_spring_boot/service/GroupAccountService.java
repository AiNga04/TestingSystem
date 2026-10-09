package org.vti.jamie.com.project_spring_boot.service;

import org.springframework.data.domain.*;
import org.vti.jamie.com.project_spring_boot.dto.response.GroupAccountResponse;

public interface GroupAccountService {
    GroupAccountResponse add(Short groupId, Short accountId);
    GroupAccountResponse getById(Short groupId, Short accountId);
    Page<GroupAccountResponse> getAll(Short groupId, String keyword, Pageable pageable);
    void remove(Short groupId, Short accountId);
}

package org.vti.jamie.com.project_spring_boot.service;

import org.springframework.data.domain.*;
import org.vti.jamie.com.project_spring_boot.dto.request.*;
import org.vti.jamie.com.project_spring_boot.dto.response.AccountResponse;

public interface AccountService {
    AccountResponse create(AccountRequest request);
    AccountResponse getById(Short id);
    Page<AccountResponse> getAll(String keyword, boolean deleted, Short departmentId, Short positionId, Pageable pageable);
    AccountResponse update(Short id, AccountUpdateRequest request);
    void delete(Short id);
    AccountResponse restore(Short id);
}

package org.vti.jamie.com.project_spring_boot.service;

import org.springframework.data.domain.*;
import org.vti.jamie.com.project_spring_boot.dto.request.GroupRequest;
import org.vti.jamie.com.project_spring_boot.dto.response.GroupResponse;

public interface GroupService {
    GroupResponse create(GroupRequest request);
    GroupResponse getById(Short id);
    Page<GroupResponse> getAll(String keyword, boolean deleted, Short creatorId, Pageable pageable);
    GroupResponse update(Short id, GroupRequest request);
    void delete(Short id);
    GroupResponse restore(Short id);
}

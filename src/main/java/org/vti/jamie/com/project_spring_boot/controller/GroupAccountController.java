package org.vti.jamie.com.project_spring_boot.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.vti.jamie.com.project_spring_boot.dto.request.GroupAccountRequest;
import org.vti.jamie.com.project_spring_boot.dto.response.*;
import org.vti.jamie.com.project_spring_boot.service.GroupAccountService;
import org.vti.jamie.com.project_spring_boot.utils.PageRequests;
import java.util.Map;

@RestController
@RequestMapping("/v1/groups/{groupId}/members")
@RequiredArgsConstructor
public class GroupAccountController {
    private final GroupAccountService service;
    private static final Map<String, String> SORTS = Map.of("accountId", "id.accountId",
            "joinedAt", "joinedAt", "username", "account.username");

    @PostMapping
    public ResponseEntity<ApiResponse<GroupAccountResponse>> add(@PathVariable @Min(1) @Max(255) Short groupId,
            @Valid @RequestBody GroupAccountRequest request) {
        var response = service.add(groupId, request.accountId());
        var location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{accountId}")
                .buildAndExpand(request.accountId()).toUri();
        return ResponseEntity.created(location).body(ApiResponse.success(201, "Thêm thành viên thành công", response));
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<ApiResponse<GroupAccountResponse>> getById(
            @PathVariable @Min(1) @Max(255) Short groupId, @PathVariable @Min(1) @Max(255) Short accountId) {
        return ResponseEntity.ok(ApiResponse.success(200, "Lấy thành viên thành công", service.getById(groupId, accountId)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<GroupAccountResponse>>> getAll(
            @PathVariable @Min(1) @Max(255) Short groupId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "accountId") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        var result = service.getAll(groupId, keyword,
                PageRequests.of(page, size, sortBy, direction, SORTS, "id.accountId"));
        return ResponseEntity.ok(ApiResponse.success(200, "Lấy danh sách thành viên thành công", PageResponse.from(result)));
    }

    @DeleteMapping("/{accountId}")
    public ResponseEntity<ApiResponse<Void>> remove(@PathVariable @Min(1) @Max(255) Short groupId,
            @PathVariable @Min(1) @Max(255) Short accountId) {
        service.remove(groupId, accountId);
        return ResponseEntity.ok(ApiResponse.success(200, "Xóa thành viên khỏi nhóm thành công", null));
    }
}

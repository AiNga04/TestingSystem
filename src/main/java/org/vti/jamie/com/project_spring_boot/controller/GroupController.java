package org.vti.jamie.com.project_spring_boot.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.vti.jamie.com.project_spring_boot.dto.request.GroupRequest;
import org.vti.jamie.com.project_spring_boot.dto.response.*;
import org.vti.jamie.com.project_spring_boot.service.GroupService;
import org.vti.jamie.com.project_spring_boot.utils.PageRequests;
import java.util.Map;

@RestController
@RequestMapping("/v1/groups")
@RequiredArgsConstructor
public class GroupController {
    private final GroupService service;
    private static final Map<String, String> SORTS = Map.of("groupId", "id", "groupName", "name", "createdAt", "createdAt");

    @PostMapping
    public ResponseEntity<ApiResponse<GroupResponse>> create(@Valid @RequestBody GroupRequest request) {
        var response = service.create(request);
        var location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(response.groupId()).toUri();
        return ResponseEntity.created(location).body(ApiResponse.success(201, "Tạo nhóm thành công", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GroupResponse>> getById(@PathVariable @Min(1) @Max(255) Short id) {
        return ResponseEntity.ok(ApiResponse.success(200, "Lấy nhóm thành công", service.getById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<GroupResponse>>> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "false") boolean deleted,
            @RequestParam(required = false) @Min(1) @Max(255) Short creatorId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "groupId") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        var result = service.getAll(keyword, deleted, creatorId,
                PageRequests.of(page, size, sortBy, direction, SORTS, "id"));
        return ResponseEntity.ok(ApiResponse.success(200, "Lấy danh sách nhóm thành công", PageResponse.from(result)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<GroupResponse>> update(@PathVariable @Min(1) @Max(255) Short id,
            @Valid @RequestBody GroupRequest request) {
        return ResponseEntity.ok(ApiResponse.success(200, "Cập nhật nhóm thành công", service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable @Min(1) @Max(255) Short id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(200, "Đã chuyển nhóm vào danh sách đã xóa", null));
    }

    @PatchMapping("/{id}/restore")
    public ResponseEntity<ApiResponse<GroupResponse>> restore(@PathVariable @Min(1) @Max(255) Short id) {
        return ResponseEntity.ok(ApiResponse.success(200, "Khôi phục nhóm thành công", service.restore(id)));
    }
}

package org.vti.jamie.com.project_spring_boot.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.vti.jamie.com.project_spring_boot.dto.request.PositionRequest;
import org.vti.jamie.com.project_spring_boot.dto.response.ApiResponse;
import org.vti.jamie.com.project_spring_boot.dto.response.PositionResponse;
import org.vti.jamie.com.project_spring_boot.dto.response.PageResponse;
import org.vti.jamie.com.project_spring_boot.service.PositionService;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/v1/positions")
@RequiredArgsConstructor
public class PositionController {
    private final PositionService service;

    @PostMapping
    public ResponseEntity<ApiResponse<PositionResponse>> create(
            @Valid @RequestBody PositionRequest request) {
        PositionResponse response = service.create(request);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(response.positionId()).toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.success(201, "Tạo chức vụ thành công", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PositionResponse>> getById(
            @PathVariable @Min(1) @Max(255) Short id) {
        return ResponseEntity.ok(ApiResponse.success(200, "Lấy chức vụ thành công", service.getById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PositionResponse>>> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "false") boolean deleted,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "positionId") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        String property = switch (sortBy) {
            case "positionId" -> "id";
            case "positionName" -> "name";
            default -> throw new IllegalArgumentException("sortBy chỉ nhận positionId hoặc positionName");
        };
        Sort.Direction sortDirection = Sort.Direction.fromOptionalString(direction)
                .orElseThrow(() -> new IllegalArgumentException("direction chỉ nhận asc hoặc desc"));
        Sort sort = Sort.by(sortDirection, property);
        if (!property.equals("id")) sort = sort.and(Sort.by("id"));
        var result = service.getAll(keyword, deleted, PageRequest.of(page, size, sort));
        return ResponseEntity.ok(ApiResponse.success(200, "Lấy danh sách chức vụ thành công", PageResponse.from(result)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PositionResponse>> update(
            @PathVariable @Min(1) @Max(255) Short id,
            @Valid @RequestBody PositionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(200, "Cập nhật chức vụ thành công", service.update(id, request)));
    }

    @PatchMapping("/{id}/restore")
    public ResponseEntity<ApiResponse<PositionResponse>> restore(
            @PathVariable @Min(1) @Max(255) Short id) {
        return ResponseEntity.ok(ApiResponse.success(200, "Khôi phục chức vụ thành công", service.restore(id)));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable @Min(1) @Max(255) Short id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(200, "Đã chuyển chức vụ vào danh sách đã xóa", null));
    }
}

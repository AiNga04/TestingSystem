package org.vti.jamie.com.project_spring_boot.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.vti.jamie.com.project_spring_boot.dto.request.DepartmentRequest;
import org.vti.jamie.com.project_spring_boot.dto.response.ApiResponse;
import org.vti.jamie.com.project_spring_boot.dto.response.DepartmentResponse;
import org.vti.jamie.com.project_spring_boot.dto.response.PageResponse;
import org.vti.jamie.com.project_spring_boot.service.DepartmentService;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/v1/departments")
@RequiredArgsConstructor
public class DepartmentController {
    private final DepartmentService service;

    @PostMapping
    public ResponseEntity<ApiResponse<DepartmentResponse>> create(
            @Valid @RequestBody DepartmentRequest request) {
        DepartmentResponse response = service.create(request);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(response.departmentId()).toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.success(201, "Tạo phòng ban thành công", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentResponse>> getById(
            @PathVariable @Min(1) @Max(255) Short id) {
        return ResponseEntity.ok(ApiResponse.success(200, "Lấy phòng ban thành công", service.getById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<DepartmentResponse>>> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "departmentId") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        String property = switch (sortBy) {
            case "departmentId" -> "id";
            case "departmentName" -> "name";
            default -> throw new IllegalArgumentException("sortBy chỉ nhận departmentId hoặc departmentName");
        };
        Sort.Direction sortDirection = Sort.Direction.fromOptionalString(direction)
                .orElseThrow(() -> new IllegalArgumentException("direction chỉ nhận asc hoặc desc"));
        Sort sort = Sort.by(sortDirection, property);
        if (!property.equals("id")) sort = sort.and(Sort.by("id"));
        var result = service.getAll(keyword, PageRequest.of(page, size, sort));
        return ResponseEntity.ok(ApiResponse.success(200, "Lấy danh sách phòng ban thành công", PageResponse.from(result)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentResponse>> update(
            @PathVariable @Min(1) @Max(255) Short id,
            @Valid @RequestBody DepartmentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(200, "Cập nhật phòng ban thành công", service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable @Min(1) @Max(255) Short id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(200, "Xóa phòng ban thành công", null));
    }
}

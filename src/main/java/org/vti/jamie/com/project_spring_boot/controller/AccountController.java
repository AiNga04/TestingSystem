package org.vti.jamie.com.project_spring_boot.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.vti.jamie.com.project_spring_boot.dto.request.*;
import org.vti.jamie.com.project_spring_boot.dto.response.*;
import org.vti.jamie.com.project_spring_boot.service.AccountService;
import org.vti.jamie.com.project_spring_boot.utils.PageRequests;
import java.util.Map;

@RestController
@RequestMapping("/v1/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService service;
    private static final Map<String, String> SORTS = Map.of("accountId", "id", "email", "email",
            "username", "username", "fullName", "fullName", "createdAt", "createdAt");

    @PostMapping
    public ResponseEntity<ApiResponse<AccountResponse>> create(@Valid @RequestBody AccountRequest request) {
        var response = service.create(request);
        var location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(response.accountId()).toUri();
        return ResponseEntity.created(location).body(ApiResponse.success(201, "Tạo tài khoản thành công", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountResponse>> getById(@PathVariable @Min(1) @Max(255) Short id) {
        return ResponseEntity.ok(ApiResponse.success(200, "Lấy tài khoản thành công", service.getById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AccountResponse>>> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "false") boolean deleted,
            @RequestParam(required = false) @Min(1) @Max(255) Short departmentId,
            @RequestParam(required = false) @Min(1) @Max(255) Short positionId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "accountId") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        var result = service.getAll(keyword, deleted, departmentId, positionId,
                PageRequests.of(page, size, sortBy, direction, SORTS, "id"));
        return ResponseEntity.ok(ApiResponse.success(200, "Lấy danh sách tài khoản thành công", PageResponse.from(result)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountResponse>> update(@PathVariable @Min(1) @Max(255) Short id,
            @Valid @RequestBody AccountUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(200, "Cập nhật tài khoản thành công", service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable @Min(1) @Max(255) Short id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(200, "Đã chuyển tài khoản vào danh sách đã xóa", null));
    }

    @PatchMapping("/{id}/restore")
    public ResponseEntity<ApiResponse<AccountResponse>> restore(@PathVariable @Min(1) @Max(255) Short id) {
        return ResponseEntity.ok(ApiResponse.success(200, "Khôi phục tài khoản thành công", service.restore(id)));
    }
}

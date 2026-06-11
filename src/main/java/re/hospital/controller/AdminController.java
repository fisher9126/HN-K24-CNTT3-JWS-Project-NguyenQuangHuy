package re.hospital.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import re.hospital.model.dto.request.CreateUserRequest;
import re.hospital.model.dto.request.UserUpdateRequest;
import re.hospital.model.dto.response.ApiResponse;
import re.hospital.model.dto.response.UserResponse;
import re.hospital.service.UserService;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {
    private final UserService userService;

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(required = false) String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<UserResponse> users = (search != null && !search.isBlank())
                ? userService.searchUsers(search, pageable)
                : userService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.<Page<UserResponse>>builder()
                .success(true).message("Lấy danh sách người dùng thành công")
                .data(users).build());
    }

    @PostMapping("/users")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody CreateUserRequest request) {
        CreateUserRequest creReq = CreateUserRequest.builder()
                .username(request.getUsername())
                .fullName(request.getFullName()).email(request.getEmail()).phone(request.getPhone()).build();
        return new ResponseEntity<>(ApiResponse.<UserResponse>builder()
                .success(true).message("Tạo người dùng thành công")
                .data(userService.createUser(creReq, request.getPassword(), request.getRoles())).build(),
                HttpStatus.CREATED);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .success(true).message("Lấy thông tin người dùng thành công")
                .data(userService.getUserById(id)).build());
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .success(true).message("Cập nhật người dùng thành công")
                .data(userService.updateUser(id, request)).build());
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.noContent().build();
    }
}

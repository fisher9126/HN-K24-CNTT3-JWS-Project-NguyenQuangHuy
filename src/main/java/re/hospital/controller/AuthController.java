package re.hospital.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import re.hospital.model.dto.request.ChangePasswordRequest;
import re.hospital.model.dto.request.LoginRequest;
import re.hospital.model.dto.request.RefreshTokenRequest;
import re.hospital.model.dto.request.RegisterRequest;
import re.hospital.model.dto.response.ApiResponse;
import re.hospital.model.dto.response.JWTResponse;
import re.hospital.model.dto.response.UserResponse;
import re.hospital.security.principal.CustomUserDetails;
import re.hospital.service.RefreshTokenService;
import re.hospital.service.UserService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse user = userService.register(request);
        return new ResponseEntity<>(ApiResponse.<UserResponse>builder()
                .success(true)
                .message("Đăng ký tài khoản thành công")
                .data(user)
                .build(), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JWTResponse>> login(@Valid @RequestBody LoginRequest request) {
        JWTResponse jwt = userService.login(request);
        return ResponseEntity.ok(ApiResponse.<JWTResponse>builder()
                .success(true)
                .message("Đăng nhập thành công")
                .data(jwt)
                .build());
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<JWTResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        JWTResponse jwt = refreshTokenService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.<JWTResponse>builder()
                .success(true)
                .message("Cấp lại access token thành công")
                .data(jwt)
                .build());
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            userService.logout(header.substring(7));
        }
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Đăng xuất thành công")
                .build());
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Đổi mật khẩu thành công")
                .build());
    }
}

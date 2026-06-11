package re.hospital.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import re.hospital.model.dto.request.LoginRequest;
import re.hospital.model.dto.request.RefreshTokenRequest;
import re.hospital.model.dto.request.RegisterRequest;
import re.hospital.model.dto.response.JWTResponse;
import re.hospital.model.dto.response.UserResponse;
import re.hospital.service.RefreshTokenService;
import re.hospital.service.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private UserService userService;
    @MockitoBean private RefreshTokenService refreshTokenService;

    @Test
    void register_ValidInput_Returns201() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser").password("123456").fullName("New User")
                .email("new@test.com").phone("0901234567").build();

        UserResponse response = UserResponse.builder()
                .id(1L).username("newuser").fullName("New User")
                .email("new@test.com").phone("0901234567").enabled(true)
                .roles(List.of("PATIENT")).build();

        when(userService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("newuser"));
    }

    @Test
    void register_MissingUsername_Returns400() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .password("123456").fullName("Test").email("t@t.com").phone("0901234567").build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_ValidCredentials_Returns200() throws Exception {
        LoginRequest request = LoginRequest.builder().username("admin").password("admin123").build();

        JWTResponse jwtResponse = JWTResponse.builder()
                .username("admin").fullName("Admin").enabled(true)
                .roles(List.of("ADMIN")).accessToken("token").refreshToken("refresh").build();

        when(userService.login(any(LoginRequest.class))).thenReturn(jwtResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("token"));
    }

    @Test
    void refreshToken_Valid_Returns200() throws Exception {
        RefreshTokenRequest request = RefreshTokenRequest.builder().refreshToken("valid-refresh").build();

        JWTResponse jwtResponse = JWTResponse.builder()
                .username("admin").accessToken("new-token").refreshToken("valid-refresh").build();

        when(refreshTokenService.refreshToken(any(RefreshTokenRequest.class))).thenReturn(jwtResponse);

        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("new-token"));
    }

    @Test
    void logout_WithToken_Returns200() throws Exception {
        doNothing().when(userService).logout(anyString());

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer some-valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Đăng xuất thành công"));
    }
}

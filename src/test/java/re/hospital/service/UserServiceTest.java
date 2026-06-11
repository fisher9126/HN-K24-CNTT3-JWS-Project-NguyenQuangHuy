package re.hospital.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import re.hospital.exception.ConflictException;
import re.hospital.exception.ResourceNotFoundException;
import re.hospital.model.dto.request.RegisterRequest;
import re.hospital.model.dto.response.UserResponse;
import re.hospital.model.entity.Role;
import re.hospital.model.entity.User;
import re.hospital.model.enums.RoleName;
import re.hospital.repository.RoleRepository;
import re.hospital.repository.TokenBlacklistRepository;
import re.hospital.repository.UserRepository;
import re.hospital.security.jwt.JWTProvider;
import re.hospital.service.impl.UserServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JWTProvider jwtProvider;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private TokenBlacklistRepository tokenBlacklistRepository;

    @InjectMocks private UserServiceImpl userService;

    @Test
    void register_Success() {
        RegisterRequest request = RegisterRequest.builder()
                .username("testuser").password("123456").fullName("Test User")
                .email("test@test.com").phone("0901234567").build();

        Role patientRole = new Role();
        patientRole.setRoleName(RoleName.PATIENT);

        User savedUser = User.builder()
                .id(1L).username("testuser").fullName("Test User")
                .email("test@test.com").phone("0901234567").enabled(true)
                .roles(List.of(patientRole)).build();

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@test.com")).thenReturn(false);
        when(userRepository.existsByPhone("0901234567")).thenReturn(false);
        when(roleRepository.findByRoleName(RoleName.PATIENT)).thenReturn(Optional.of(patientRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse result = userService.register(request);

        assertEquals("testuser", result.getUsername());
        assertEquals("test@test.com", result.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_DuplicateUsername_ThrowsConflict() {
        RegisterRequest request = RegisterRequest.builder()
                .username("existing").password("123456").fullName("Test")
                .email("new@test.com").phone("0901234567").build();

        when(userRepository.existsByUsername("existing")).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.register(request));
    }

    @Test
    void register_DuplicateEmail_ThrowsConflict() {
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser").password("123456").fullName("Test")
                .email("existing@test.com").phone("0901234567").build();

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.register(request));
    }

    @Test
    void getUserById_Found_ReturnsUser() {
        Role role = new Role();
        role.setRoleName(RoleName.PATIENT);

        User user = User.builder()
                .id(1L).username("testuser").fullName("Test")
                .email("test@test.com").phone("0901234567").enabled(true)
                .roles(List.of(role)).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse result = userService.getUserById(1L);

        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void getUserById_NotFound_ThrowsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(999L));
    }
}

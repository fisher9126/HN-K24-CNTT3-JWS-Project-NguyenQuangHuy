package re.hospital.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import re.hospital.exception.ConflictException;
import re.hospital.exception.ResourceNotFoundException;
import re.hospital.model.dto.request.LoginRequest;
import re.hospital.model.dto.request.RegisterRequest;
import re.hospital.model.dto.response.JWTResponse;
import re.hospital.model.dto.response.UserResponse;
import re.hospital.model.entity.RefreshToken;
import re.hospital.model.entity.Role;
import re.hospital.model.entity.User;
import re.hospital.model.enums.RoleName;
import re.hospital.repository.RoleRepository;
import re.hospital.repository.UserRepository;
import re.hospital.security.jwt.JWTProvider;
import re.hospital.security.principal.CustomUserDetails;
import re.hospital.service.impl.UserServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserService {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JWTProvider jwtProvider;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private TokenBlacklistService tokenBlacklistService;

    private Role patientRole;
    private User testUser;

    @BeforeEach
    void setUp() {
        patientRole = Role.builder().id(1L).roleName(RoleName.PATIENT).build();
        testUser = User.builder()
                .id(1L).username("testuser").password("encoded").fullName("Test User")
                .email("test@test.com").phone("0901234567").enabled(true)
                .roles(List.of(patientRole)).build();
    }

    @Test
    void register_Success() {
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser").password("123456").fullName("New User")
                .email("new@test.com").phone("0909999999").build();

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(userRepository.existsByPhone("0909999999")).thenReturn(false);
        when(roleRepository.findByRoleName(RoleName.PATIENT)).thenReturn(Optional.of(patientRole));
        when(passwordEncoder.encode("123456")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse result = userService.register(request);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_DuplicateUsername_ThrowsConflict() {
        RegisterRequest request = RegisterRequest.builder()
                .username("testuser").password("123456").fullName("Test")
                .email("new@test.com").phone("0909999999").build();

        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.register(request));
    }

    @Test
    void login_Success() {
        LoginRequest request = LoginRequest.builder().username("testuser").password("123456").build();
        CustomUserDetails userDetails = CustomUserDetails.builder()
                .id(1L).username("testuser").fullName("Test User").enabled(true)
                .authorities(List.of(new SimpleGrantedAuthority("PATIENT")))
                .build();
        Authentication auth = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(jwtProvider.generateToken("testuser")).thenReturn("access-token");
        when(refreshTokenService.createRefreshToken("testuser"))
                .thenReturn(RefreshToken.builder().token("refresh-token").build());

        JWTResponse result = userService.login(request);

        assertEquals("testuser", result.getUsername());
        assertEquals("access-token", result.getAccessToken());
        assertEquals("refresh-token", result.getRefreshToken());
    }

    @Test
    void getUserById_NotFound_ThrowsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(999L));
    }

    @Test
    void deactivateUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.deactivateUser(1L);

        assertFalse(testUser.getEnabled());
        verify(userRepository).save(testUser);
    }
}

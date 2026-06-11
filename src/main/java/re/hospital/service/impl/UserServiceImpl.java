package re.hospital.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import re.hospital.exception.BadRequestException;
import re.hospital.exception.ConflictException;
import re.hospital.exception.ResourceNotFoundException;
import re.hospital.model.dto.request.*;
import re.hospital.model.dto.response.JWTResponse;
import re.hospital.model.dto.response.UserResponse;
import re.hospital.model.entity.Role;
import re.hospital.model.entity.TokenBlacklist;
import re.hospital.model.entity.User;
import re.hospital.model.enums.RoleName;
import re.hospital.repository.RoleRepository;
import re.hospital.repository.TokenBlacklistRepository;
import re.hospital.repository.UserRepository;
import re.hospital.security.jwt.JWTProvider;
import re.hospital.security.principal.CustomUserDetails;
import re.hospital.service.RefreshTokenService;
import re.hospital.service.UserService;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JWTProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistRepository tokenBlacklistRepository;


    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request, String password, List<String> roleNames) {
        if (userRepository.existsByEmail(request.getEmail()))
            throw new ConflictException("Email đã được sử dụng");
        if (userRepository.existsByUsername(request.getUsername()))
            throw new ConflictException("Username đã được sử dụng");

        List<Role> roles = roleNames.stream()
                .map(name -> roleRepository.findByRoleName(RoleName.valueOf(name))
                        .orElseThrow(() -> new ResourceNotFoundException("Role " + name + " không tồn tại")))
                .toList();

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(password))
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .enabled(true)
                .roles(roles)
                .build();

        return toUserResponse(userRepository.save(user));
    }

    @Override
    public Page<UserResponse> searchUsers(String keyword, Pageable pageable) {
        return userRepository.searchUsers(keyword, pageable).map(this::toUserResponse);
    }

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Username '" + request.getUsername() + "' đã tồn tại");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email '" + request.getEmail() + "' đã được sử dụng");
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new ConflictException("Số điện thoại '" + request.getPhone() + "' đã được sử dụng");
        }

        Role patientRole = roleRepository.findByRoleName(RoleName.PATIENT)
                .orElseThrow(() -> new ResourceNotFoundException("Role PATIENT không tồn tại"));

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .enabled(true)
                .roles(List.of(patientRole))
                .build();

        User saved = userRepository.save(user);
        return toUserResponse(saved);
    }

    @Override
    public JWTResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String accessToken = jwtProvider.generateToken(userDetails.getUsername());
        String refreshToken = refreshTokenService.createRefreshToken(userDetails.getUsername()).getToken();

        return JWTResponse.builder()
                .username(userDetails.getUsername())
                .fullName(userDetails.getFullName())
                .enabled(userDetails.getEnabled())
                .roles(userDetails.getAuthorities().stream()
                        .map(Object::toString).toList())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    @Transactional
    public void logout(String token) {
        Instant expiry = jwtProvider.getExpirationFromToken(token);
        TokenBlacklist blacklist = TokenBlacklist.builder()
                .token(token)
                .expiryDate(expiry)
                .blacklistedAt(Instant.now())
                .build();
        tokenBlacklistRepository.save(blacklist);
    }

    @Override
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BadRequestException("Mật khẩu cũ không đúng");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toUserResponse);
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User ID " + id + " không tồn tại"));
        return toUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User ID " + id + " không tồn tại"));

        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getEmail() != null) {
            if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
                throw new ConflictException("Email đã được sử dụng");
            }
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            if (!user.getPhone().equals(request.getPhone()) && userRepository.existsByPhone(request.getPhone())) {
                throw new ConflictException("Số điện thoại đã được sử dụng");
            }
            user.setPhone(request.getPhone());
        }
        if (request.getEnabled() != null) user.setEnabled(request.getEnabled());
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            List<Role> roles = request.getRoles().stream()
                    .map(roleName -> roleRepository.findByRoleName(RoleName.valueOf(roleName))
                            .orElseThrow(() -> new ResourceNotFoundException("Role " + roleName + " không tồn tại")))
                    .toList();
            user.setRoles(roles);
        }

        return toUserResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User ID " + id + " không tồn tại"));
        user.setEnabled(false);
        userRepository.save(user);
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .enabled(user.getEnabled())
                .roles(user.getRoles().stream()
                        .map(role -> role.getRoleName().name()).toList())
                .build();
    }
}

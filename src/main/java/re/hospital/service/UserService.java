package re.hospital.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import re.hospital.model.dto.request.*;
import re.hospital.model.dto.response.JWTResponse;
import re.hospital.model.dto.response.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse register(RegisterRequest request);
    JWTResponse login(LoginRequest request);
    void logout(String token);
    void changePassword(String username, ChangePasswordRequest request);
    Page<UserResponse> getAllUsers(Pageable pageable);
    UserResponse getUserById(Long id);
    UserResponse updateUser(Long id, UserUpdateRequest request);
    void deactivateUser(Long id);
    UserResponse createUser(UserUpdateRequest request, String password, List<String> roles);
    Page<UserResponse> searchUsers(String keyword, Pageable pageable);

}

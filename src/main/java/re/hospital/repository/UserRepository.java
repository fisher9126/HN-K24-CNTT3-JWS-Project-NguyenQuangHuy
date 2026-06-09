package re.hospital.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import re.hospital.model.entity.User;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    @Query("SELECT u FROM User u WHERE u.fullName LIKE %:keyword% OR u.username LIKE %:keyword% OR u.email LIKE %:keyword%")
    Page<User> searchUsers(String keyword, Pageable pageable);

}

package re.hospital.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import re.hospital.model.entity.RefreshToken;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    List<RefreshToken> findByUsername(String username);
    void deleteByUsername(String username);
}

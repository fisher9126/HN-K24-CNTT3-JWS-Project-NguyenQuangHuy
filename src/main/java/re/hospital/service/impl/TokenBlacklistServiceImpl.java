package re.hospital.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import re.hospital.model.entity.TokenBlacklist;
import re.hospital.repository.TokenBlacklistRepository;
import re.hospital.service.TokenBlacklistService;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TokenBlacklistServiceImpl implements TokenBlacklistService {
    private final TokenBlacklistRepository tokenBlacklistRepository;

    @Override
    public void blacklist(String token, long expirationInSeconds) {
        TokenBlacklist blacklist = TokenBlacklist.builder()
                .token(token)
                .expiryDate(Instant.now().plusSeconds(expirationInSeconds))
                .blacklistedAt(Instant.now())
                .build();
        tokenBlacklistRepository.save(blacklist);
    }

    @Override
    public boolean isBlacklisted(String token) {
        return tokenBlacklistRepository.existsByToken(token);
    }
}

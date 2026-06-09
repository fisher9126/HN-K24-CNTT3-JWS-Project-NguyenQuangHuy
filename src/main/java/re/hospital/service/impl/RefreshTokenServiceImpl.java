package re.hospital.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import re.hospital.exception.BadRequestException;
import re.hospital.exception.ResourceNotFoundException;
import re.hospital.model.dto.request.RefreshTokenRequest;
import re.hospital.model.dto.response.JWTResponse;
import re.hospital.model.entity.RefreshToken;
import re.hospital.repository.RefreshTokenRepository;
import re.hospital.security.jwt.JWTProvider;
import re.hospital.security.principal.CustomUserDetails;
import re.hospital.service.RefreshTokenService;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final JWTProvider jwtProvider;
    private final UserDetailsService userDetailsService;

    @Value("${jwt-refresh-expire}")
    private Long refreshExpired;

    @Override
    @Transactional
    public RefreshToken createRefreshToken(String username) {
        refreshTokenRepository.findByUsername(username).stream()
                .filter(rt -> !rt.isRevoked())
                .forEach(rt -> {
                    rt.setRevoked(true);
                    refreshTokenRepository.save(rt);
                });

        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshExpired))
                .username(username)
                .revoked(false)
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.isRevoked()) {
            throw new BadRequestException("Refresh token đã bị thu hồi");
        }
        if (token.getExpiryDate().isBefore(Instant.now())) {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
            throw new BadRequestException("Refresh token đã hết hạn");
        }
        return token;
    }

    @Override
    @Transactional
    public JWTResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token không tồn tại"));

        verifyExpiration(refreshToken);

        String newAccessToken = jwtProvider.generateToken(refreshToken.getUsername());
        CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(refreshToken.getUsername());

        return JWTResponse.builder()
                .username(userDetails.getUsername())
                .fullName(userDetails.getFullName())
                .enabled(userDetails.getEnabled())
                .roles(userDetails.getAuthorities().stream()
                        .map(Object::toString).toList())
                .accessToken(newAccessToken)
                .refreshToken(refreshToken.getToken())
                .build();
    }
}

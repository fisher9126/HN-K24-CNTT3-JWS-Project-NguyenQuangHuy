package re.hospital.service;

import re.hospital.model.dto.request.RefreshTokenRequest;
import re.hospital.model.dto.response.JWTResponse;
import re.hospital.model.entity.RefreshToken;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(String username);
    RefreshToken verifyExpiration(RefreshToken token);
    JWTResponse refreshToken(RefreshTokenRequest request);
}

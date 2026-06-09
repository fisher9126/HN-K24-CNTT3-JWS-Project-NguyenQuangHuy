package re.hospital.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
@Slf4j
public class JWTProvider {
    @Value("${jwt-secret}")
    private String jwtSecret;

    @Value("${jwt-expire}")
    private long jwtExpire;

    public String generateToken(String username) {
        SecretKey key = getSigningKey();
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + jwtExpire);
        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expireDate)
                .signWith(key)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT đã hết hạn");
        } catch (MalformedJwtException e) {
            log.warn("JWT không đúng định dạng");
        } catch (UnsupportedJwtException e) {
            log.warn("JWT không được hỗ trợ");
        } catch (IllegalArgumentException e) {
            log.warn("JWT rỗng");
        } catch (JwtException e) {
            log.warn("JWT không hợp lệ");
        }
        return false;
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public Instant getExpirationFromToken(String token) {
        Date expiration = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();
        return expiration.toInstant();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
}

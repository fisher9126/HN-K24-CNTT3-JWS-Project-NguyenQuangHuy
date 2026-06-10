package re.hospital.service;

public interface TokenBlacklistService {
    void blacklist(String token, long expirationInSeconds);
    boolean isBlacklisted(String token);
}

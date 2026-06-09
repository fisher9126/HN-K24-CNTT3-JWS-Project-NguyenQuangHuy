package re.hospital.model.dto.response;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class JWTResponse {
    private String username;
    private String fullName;
    private Boolean enabled;
    private List<String> roles;
    private String accessToken;
    private String refreshToken;
}

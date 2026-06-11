package re.hospital.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserUpdateRequest {
    @Size(max = 70, message = "Họ tên tối đa 70 ký tự")
    private String fullName;

    @Email(message = "Email không đúng định dạng")
    private String email;
    @Pattern(regexp = "^0\\d{9,10}$", message = "Số điện thoại không hợp lệ")
    private String phone;


    private Boolean enabled;

    private List<String> roles;
}

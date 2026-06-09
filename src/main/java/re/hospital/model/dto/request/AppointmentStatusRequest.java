package re.hospital.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import re.hospital.model.enums.AppointmentStatus;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AppointmentStatusRequest {
    @NotNull(message = "Trạng thái không được để trống")
    private AppointmentStatus status;
}

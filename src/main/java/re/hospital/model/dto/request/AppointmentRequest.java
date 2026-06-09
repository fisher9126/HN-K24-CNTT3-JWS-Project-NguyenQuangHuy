package re.hospital.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AppointmentRequest {
    @NotNull(message = "Doctor ID không được để trống")
    private Long doctorId;

    @NotNull(message = "Thời gian khám không được để trống")
    private LocalDateTime timeSlot;

    private String note;
}

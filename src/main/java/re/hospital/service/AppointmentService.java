package re.hospital.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import re.hospital.model.dto.request.AppointmentRequest;
import re.hospital.model.dto.request.AppointmentStatusRequest;
import re.hospital.model.dto.response.AppointmentResponse;

public interface AppointmentService {
    AppointmentResponse createAppointment(String patientUsername, AppointmentRequest request);
    Page<AppointmentResponse> getPatientAppointments(String patientUsername, Pageable pageable);
    Page<AppointmentResponse> getDoctorAppointments(String doctorUsername, Pageable pageable);
    AppointmentResponse updateAppointmentStatus(Long appointmentId, String doctorUsername, AppointmentStatusRequest request);
    AppointmentResponse cancelAppointment(Long appointmentId, String patientUsername);
}

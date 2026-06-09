package re.hospital.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import re.hospital.exception.BadRequestException;
import re.hospital.exception.ConflictException;
import re.hospital.exception.ResourceNotFoundException;
import re.hospital.model.dto.request.AppointmentRequest;
import re.hospital.model.dto.request.AppointmentStatusRequest;
import re.hospital.model.dto.response.AppointmentResponse;
import re.hospital.model.entity.Appointment;
import re.hospital.model.entity.User;
import re.hospital.model.enums.AppointmentStatus;
import re.hospital.model.enums.RoleName;
import re.hospital.repository.AppointmentRepository;
import re.hospital.repository.UserRepository;
import re.hospital.service.AppointmentService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public AppointmentResponse createAppointment(String patientUsername, AppointmentRequest request) {
        User patient = userRepository.findByUsername(patientUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Patient không tồn tại"));

        User doctor = userRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor ID " + request.getDoctorId() + " không tồn tại"));

        boolean isDoctor = doctor.getRoles().stream()
                .anyMatch(r -> r.getRoleName() == RoleName.DOCTOR);
        if (!isDoctor) {
            throw new BadRequestException("User ID " + request.getDoctorId() + " không phải bác sĩ");
        }

        if (request.getTimeSlot().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Thời gian khám phải ở tương lai");
        }

        LocalDateTime start = request.getTimeSlot().minusMinutes(30);
        LocalDateTime end = request.getTimeSlot().plusMinutes(30);
        List<Appointment> conflicts = appointmentRepository.findByDoctorAndTimeSlotBetween(doctor, start, end);
        boolean hasConflict = conflicts.stream()
                .anyMatch(a -> a.getStatus() != AppointmentStatus.REJECTED && a.getStatus() != AppointmentStatus.CANCELLED);
        if (hasConflict) {
            throw new ConflictException("Bác sĩ đã có lịch khám trùng vào thời gian này");
        }

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .timeSlot(request.getTimeSlot())
                .status(AppointmentStatus.PENDING)
                .note(request.getNote())
                .build();

        return toResponse(appointmentRepository.save(appointment));
    }

    @Override
    public Page<AppointmentResponse> getPatientAppointments(String patientUsername, Pageable pageable) {
        User patient = userRepository.findByUsername(patientUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Patient không tồn tại"));
        return appointmentRepository.findByPatient(patient, pageable).map(this::toResponse);
    }

    @Override
    public Page<AppointmentResponse> getDoctorAppointments(String doctorUsername, Pageable pageable) {
        User doctor = userRepository.findByUsername(doctorUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor không tồn tại"));
        return appointmentRepository.findByDoctor(doctor, pageable).map(this::toResponse);
    }

    @Override
    @Transactional
    public AppointmentResponse updateAppointmentStatus(Long appointmentId, String doctorUsername, AppointmentStatusRequest request) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment ID " + appointmentId + " không tồn tại"));

        if (!appointment.getDoctor().getUsername().equals(doctorUsername)) {
            throw new BadRequestException("Bạn không có quyền cập nhật lịch khám này");
        }

        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new BadRequestException("Chỉ có thể cập nhật lịch khám đang ở trạng thái PENDING");
        }

        AppointmentStatus newStatus = request.getStatus();
        if (newStatus != AppointmentStatus.APPROVED && newStatus != AppointmentStatus.REJECTED
                && newStatus != AppointmentStatus.COMPLETED) {
            throw new BadRequestException("Trạng thái không hợp lệ. Chỉ chấp nhận: APPROVED, REJECTED, COMPLETED");
        }

        appointment.setStatus(newStatus);
        return toResponse(appointmentRepository.save(appointment));
    }

    @Override
    @Transactional
    public AppointmentResponse cancelAppointment(Long appointmentId, String patientUsername) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment ID " + appointmentId + " không tồn tại"));

        if (!appointment.getPatient().getUsername().equals(patientUsername)) {
            throw new BadRequestException("Bạn không có quyền hủy lịch khám này");
        }

        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new BadRequestException("Chỉ có thể hủy lịch khám đang ở trạng thái PENDING");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        return toResponse(appointmentRepository.save(appointment));
    }

    private AppointmentResponse toResponse(Appointment a) {
        return AppointmentResponse.builder()
                .id(a.getId())
                .patientId(a.getPatient().getId())
                .patientName(a.getPatient().getFullName())
                .doctorId(a.getDoctor().getId())
                .doctorName(a.getDoctor().getFullName())
                .timeSlot(a.getTimeSlot())
                .status(a.getStatus())
                .note(a.getNote())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}

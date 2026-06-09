package re.hospital.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import re.hospital.model.dto.request.AppointmentRequest;
import re.hospital.model.dto.response.ApiResponse;
import re.hospital.model.dto.response.AppointmentResponse;
import re.hospital.model.dto.response.MedicalRecordResponse;
import re.hospital.security.principal.CustomUserDetails;
import re.hospital.service.AppointmentService;
import re.hospital.service.MedicalRecordService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/patient")
@RequiredArgsConstructor
public class PatientController {
    private final AppointmentService appointmentService;
    private final MedicalRecordService medicalRecordService;

    @PostMapping("/appointments")
    public ResponseEntity<ApiResponse<AppointmentResponse>> createAppointment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AppointmentRequest request) {
        AppointmentResponse appointment = appointmentService.createAppointment(userDetails.getUsername(), request);
        return new ResponseEntity<>(ApiResponse.<AppointmentResponse>builder()
                .success(true)
                .message("Đặt lịch khám thành công")
                .data(appointment)
                .build(), HttpStatus.CREATED);
    }

    @GetMapping("/appointments")
    public ResponseEntity<ApiResponse<Page<AppointmentResponse>>> getMyAppointments(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timeSlot").descending());
        return ResponseEntity.ok(ApiResponse.<Page<AppointmentResponse>>builder()
                .success(true)
                .message("Lấy lịch sử khám bệnh thành công")
                .data(appointmentService.getPatientAppointments(userDetails.getUsername(), pageable))
                .build());
    }

    @PutMapping("/appointments/{id}/cancel")
    public ResponseEntity<ApiResponse<AppointmentResponse>> cancelAppointment(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.<AppointmentResponse>builder()
                .success(true)
                .message("Hủy lịch khám thành công")
                .data(appointmentService.cancelAppointment(id, userDetails.getUsername()))
                .build());
    }

    @GetMapping("/records")
    public ResponseEntity<ApiResponse<List<MedicalRecordResponse>>> getMyRecords(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.<List<MedicalRecordResponse>>builder()
                .success(true)
                .message("Lấy hồ sơ bệnh án thành công")
                .data(medicalRecordService.getPatientRecords(userDetails.getUsername()))
                .build());
    }
}

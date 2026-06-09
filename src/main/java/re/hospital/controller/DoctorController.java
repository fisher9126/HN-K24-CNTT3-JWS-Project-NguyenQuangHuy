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
import org.springframework.web.multipart.MultipartFile;
import re.hospital.model.dto.request.AppointmentStatusRequest;
import re.hospital.model.dto.response.ApiResponse;
import re.hospital.model.dto.response.AppointmentResponse;
import re.hospital.model.dto.response.MedicalRecordResponse;
import re.hospital.security.principal.CustomUserDetails;
import re.hospital.service.AppointmentService;
import re.hospital.service.MedicalRecordService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/doctor")
@RequiredArgsConstructor
public class DoctorController {
    private final AppointmentService appointmentService;
    private final MedicalRecordService medicalRecordService;

    @GetMapping("/appointments")
    public ResponseEntity<ApiResponse<Page<AppointmentResponse>>> getMyAppointments(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timeSlot").descending());
        return ResponseEntity.ok(ApiResponse.<Page<AppointmentResponse>>builder()
                .success(true)
                .message("Lấy danh sách lịch khám thành công")
                .data(appointmentService.getDoctorAppointments(userDetails.getUsername(), pageable))
                .build());
    }

    @PutMapping("/appointments/{id}/status")
    public ResponseEntity<ApiResponse<AppointmentResponse>> updateStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AppointmentStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.<AppointmentResponse>builder()
                .success(true)
                .message("Cập nhật trạng thái lịch khám thành công")
                .data(appointmentService.updateAppointmentStatus(id, userDetails.getUsername(), request))
                .build());
    }

    @PostMapping("/records/upload")
    public ResponseEntity<ApiResponse<MedicalRecordResponse>> uploadRecord(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long appointmentId,
            @RequestParam(required = false) String diagnosis,
            @RequestParam(required = false) String prescription,
            @RequestParam(required = false) MultipartFile file) {
        MedicalRecordResponse record = medicalRecordService.uploadRecord(
                userDetails.getUsername(), appointmentId, diagnosis, prescription, file);
        return ResponseEntity.ok(ApiResponse.<MedicalRecordResponse>builder()
                .success(true)
                .message("Tải hồ sơ bệnh án thành công")
                .data(record)
                .build());
    }

    @GetMapping("/records")
    public ResponseEntity<ApiResponse<List<MedicalRecordResponse>>> getMyRecords(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.<List<MedicalRecordResponse>>builder()
                .success(true)
                .message("Lấy danh sách hồ sơ bệnh án thành công")
                .data(medicalRecordService.getDoctorRecords(userDetails.getUsername()))
                .build());
    }
}

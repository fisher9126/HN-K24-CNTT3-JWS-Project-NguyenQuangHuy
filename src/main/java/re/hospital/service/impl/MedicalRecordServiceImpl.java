package re.hospital.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import re.hospital.exception.BadRequestException;
import re.hospital.exception.ResourceNotFoundException;
import re.hospital.model.dto.response.MedicalRecordResponse;
import re.hospital.model.entity.Appointment;
import re.hospital.model.entity.MedicalRecord;
import re.hospital.model.entity.User;
import re.hospital.model.enums.AppointmentStatus;
import re.hospital.repository.AppointmentRepository;
import re.hospital.repository.MedicalRecordRepository;
import re.hospital.repository.UserRepository;
import re.hospital.service.CloudinaryService;
import re.hospital.service.MedicalRecordService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicalRecordServiceImpl implements MedicalRecordService {
    private final MedicalRecordRepository medicalRecordRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public MedicalRecordResponse uploadRecord(String doctorUsername, Long appointmentId,
                                               String diagnosis, String prescription, MultipartFile file) {
        User doctor = userRepository.findByUsername(doctorUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor không tồn tại"));

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment ID " + appointmentId + " không tồn tại"));

        if (!appointment.getDoctor().getId().equals(doctor.getId())) {
            throw new BadRequestException("Bạn không phải bác sĩ của lịch khám này");
        }

        if (appointment.getStatus() != AppointmentStatus.APPROVED && appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new BadRequestException("Chỉ có thể tải hồ sơ cho lịch khám đã được duyệt hoặc hoàn thành");
        }

        String fileUrl = null;
        String fileName = null;
        if (file != null && !file.isEmpty()) {
            fileUrl = cloudinaryService.uploadFile(file);
            fileName = file.getOriginalFilename();
        }

        MedicalRecord record = MedicalRecord.builder()
                .appointment(appointment)
                .doctor(doctor)
                .patient(appointment.getPatient())
                .diagnosis(diagnosis)
                .prescription(prescription)
                .fileUrl(fileUrl)
                .fileName(fileName)
                .build();

        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(appointment);

        return toResponse(medicalRecordRepository.save(record));
    }

    @Override
    public List<MedicalRecordResponse> getPatientRecords(String patientUsername) {
        User patient = userRepository.findByUsername(patientUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Patient không tồn tại"));
        return medicalRecordRepository.findByPatient(patient).stream()
                .map(this::toResponse).toList();
    }

    @Override
    public List<MedicalRecordResponse> getDoctorRecords(String doctorUsername) {
        User doctor = userRepository.findByUsername(doctorUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor không tồn tại"));
        return medicalRecordRepository.findByDoctor(doctor).stream()
                .map(this::toResponse).toList();
    }

    private MedicalRecordResponse toResponse(MedicalRecord r) {
        return MedicalRecordResponse.builder()
                .id(r.getId())
                .appointmentId(r.getAppointment().getId())
                .doctorId(r.getDoctor().getId())
                .doctorName(r.getDoctor().getFullName())
                .patientId(r.getPatient().getId())
                .patientName(r.getPatient().getFullName())
                .diagnosis(r.getDiagnosis())
                .prescription(r.getPrescription())
                .fileUrl(r.getFileUrl())
                .fileName(r.getFileName())
                .createdAt(r.getCreatedAt())
                .build();
    }
}

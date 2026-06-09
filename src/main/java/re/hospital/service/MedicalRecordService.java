package re.hospital.service;

import org.springframework.web.multipart.MultipartFile;
import re.hospital.model.dto.response.MedicalRecordResponse;

import java.util.List;

public interface MedicalRecordService {
    MedicalRecordResponse uploadRecord(String doctorUsername, Long appointmentId,
                                       String diagnosis, String prescription, MultipartFile file);
    List<MedicalRecordResponse> getPatientRecords(String patientUsername);
    List<MedicalRecordResponse> getDoctorRecords(String doctorUsername);
}

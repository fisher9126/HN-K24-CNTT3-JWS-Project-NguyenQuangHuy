package re.hospital.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import re.hospital.model.entity.Appointment;
import re.hospital.model.entity.User;
import re.hospital.model.enums.AppointmentStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    Page<Appointment> findByPatient(User patient, Pageable pageable);
    Page<Appointment> findByDoctor(User doctor, Pageable pageable);
    List<Appointment> findByDoctorAndTimeSlotBetween(User doctor, LocalDateTime start, LocalDateTime end);
    Page<Appointment> findByDoctorAndStatus(User doctor, AppointmentStatus status, Pageable pageable);
}

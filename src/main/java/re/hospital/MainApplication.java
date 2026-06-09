package re.hospital;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import re.hospital.model.entity.Role;
import re.hospital.model.entity.User;
import re.hospital.model.enums.RoleName;
import re.hospital.repository.RoleRepository;
import re.hospital.repository.UserRepository;

import java.util.List;
import java.util.Map;

@SpringBootApplication
public class MainApplication {

    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public Cloudinary cloudinary(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret) {
        return new Cloudinary(Map.of(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        ));
    }

    @Bean
    CommandLineRunner initData(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder encoder) {
        return args -> {
            if (roleRepository.count() == 0) {
                Role admin = roleRepository.save(Role.builder().roleName(RoleName.ADMIN).build());
                Role doctor = roleRepository.save(Role.builder().roleName(RoleName.DOCTOR).build());
                Role patient = roleRepository.save(Role.builder().roleName(RoleName.PATIENT).build());

                if (userRepository.count() == 0) {
                    userRepository.save(User.builder()
                            .username("admin")
                            .password(encoder.encode("admin123"))
                            .fullName("System Admin")
                            .email("admin@hospital.re")
                            .phone("0900000001")
                            .enabled(true)
                            .roles(List.of(admin))
                            .build());

                    userRepository.save(User.builder()
                            .username("doctor1")
                            .password(encoder.encode("doctor123"))
                            .fullName("Bác sĩ Nguyễn Văn A")
                            .email("doctor1@hospital.re")
                            .phone("0900000002")
                            .enabled(true)
                            .roles(List.of(doctor))
                            .build());

                    userRepository.save(User.builder()
                            .username("patient1")
                            .password(encoder.encode("patient123"))
                            .fullName("Bệnh nhân Trần Thị B")
                            .email("patient1@hospital.re")
                            .phone("0900000003")
                            .enabled(true)
                            .roles(List.of(patient))
                            .build());
                }
            }
        };
    }
}

package br.ufscar.pescd.config;

import br.ufscar.pescd.entity.User;
import br.ufscar.pescd.enums.UserRole;
import br.ufscar.pescd.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setFullName("Administrador");
            admin.setEmail("admin@pescd.com");
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(UserRole.ADMIN);
            admin.setEnabled(true);

            userRepository.save(admin);
        }

        if (!userRepository.existsByUsername("secretario")) {
            User secretario = new User();
            secretario.setFullName("Secretário");
            secretario.setEmail("secretario@pescd.com");
            secretario.setUsername("secretario");
            secretario.setPassword(passwordEncoder.encode("123456"));
            secretario.setRole(UserRole.SECRETARIO);
            secretario.setEnabled(true);

            userRepository.save(secretario);
        }

        if (!userRepository.existsByUsername("professor")) {
            User professor = new User();
            professor.setFullName("Professor Teste");
            professor.setEmail("professor@pescd.com");
            professor.setUsername("professor");
            professor.setPassword(passwordEncoder.encode("123456"));
            professor.setRole(UserRole.PROFESSOR);
            professor.setEnabled(true);

            userRepository.save(professor);
        }
    }
}
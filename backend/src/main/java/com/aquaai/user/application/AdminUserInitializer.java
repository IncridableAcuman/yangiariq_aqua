package com.aquaai.user.application;

import com.aquaai.auth.application.AuthService;
import com.aquaai.shared.security.UserRole;
import com.aquaai.user.domain.User;
import com.aquaai.user.domain.UserRepository;
import com.aquaai.user.domain.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ensures designated administrator accounts are provisioned and granted ADMIN platform role.
 */
@Slf4j
@Component
@Order(10)
@RequiredArgsConstructor
public class AdminUserInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("Checking designated admin accounts provisioning...");

        ensureAdminAccount(
                "abdusharipovizzat03@gmail.com",
                "+998901234501",
                "Izzatbek",
                "Abdusharipov"
        );

        ensureAdminAccount(
                "izzatbek.engineer@gmail.com",
                "+998901234502",
                "Izzatbek",
                "Engineer"
        );
    }

    private void ensureAdminAccount(String email, String defaultPhone, String firstName, String lastName) {
        String normalizedEmail = email.trim().toLowerCase();

        userRepository.findByEmail(normalizedEmail).ifPresentOrElse(
                existingUser -> {
                    if (existingUser.getRole() != UserRole.ADMIN && existingUser.getRole() != UserRole.SUPER_ADMIN) {
                        log.info("Elevating existing user account ({}) to ADMIN role", normalizedEmail);
                        existingUser.setRole(UserRole.ADMIN);
                        userRepository.save(existingUser);
                    } else {
                        log.debug("Designated admin account ({}) already has administrative role: {}", normalizedEmail, existingUser.getRole());
                    }
                },
                () -> {
                    // Check if default phone is already taken by another account, generate unique if so
                    String phone = defaultPhone;
                    if (userRepository.existsByPhoneNumber(phone)) {
                        phone = "+99890" + (Math.abs(normalizedEmail.hashCode()) % 9000000 + 1000000);
                    }

                    log.info("Provisioning baseline designated admin account for: {}", normalizedEmail);
                    User adminUser = User.builder()
                            .email(normalizedEmail)
                            .phoneNumber(phone)
                            .firstName(firstName)
                            .lastName(lastName)
                            .fullName(firstName + " " + lastName)
                            .passwordHash(passwordEncoder.encode("Admin123!"))
                            .role(UserRole.ADMIN)
                            .status(UserStatus.ACTIVE)
                            .district("Yangiariq")
                            .build();

                    userRepository.save(adminUser);
                    log.info("Baseline admin account successfully provisioned: {} ({})", normalizedEmail, phone);
                }
        );
    }
}

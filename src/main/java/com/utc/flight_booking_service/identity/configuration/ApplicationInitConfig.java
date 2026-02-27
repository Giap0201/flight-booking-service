package com.utc.flight_booking_service.identity.configuration;

import com.utc.flight_booking_service.identity.domain.entities.Role;
import com.utc.flight_booking_service.identity.domain.entities.User;
import com.utc.flight_booking_service.identity.domain.repository.RoleRepository;
import com.utc.flight_booking_service.identity.domain.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {

    @NonFinal
    static final String ADMIN_EMAIL = "admin@system.com";

    @NonFinal
    static final String ADMIN_PASSWORD = "admin";
    PasswordEncoder passwordEncoder;

    @Bean
//    @ConditionalOnProperty(
//            prefix = "spring",
//            value = "datasource.driverClassName",
//            havingValue = "com.mysql.cj.jdbc.Driver")
    ApplicationRunner applicationRunner(UserRepository userRepository, RoleRepository roleRepository) {
        log.info("Initializing application.....");
        return args -> {

            Role userRole = roleRepository.findByName("USER")
                    .orElseGet(() -> roleRepository.save(
                            Role.builder()
                                    .name("USER")
                                    .description("Default user role")
                                    .build()
                    ));

            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseGet(() -> roleRepository.save(
                            Role.builder()
                                    .name("ADMIN")
                                    .description("Administrator role")
                                    .build()
                    ));

            if (userRepository.findByEmail(ADMIN_EMAIL).isEmpty()) {

                var roles = new HashSet<Role>();
                roles.add(userRole);
                roles.add(adminRole);

                User admin = User.builder()
                        .email(ADMIN_EMAIL)
                        .fullName("admin")
                        .password(passwordEncoder.encode(ADMIN_PASSWORD))
                        .roles(roles)
                        .build();

                userRepository.save(admin);

                log.warn("Admin account created. Please change default password.");
            }
        };
    }
}

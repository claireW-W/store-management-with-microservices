package com.store.store.config;

import com.store.store.model.User;
import com.store.store.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Component
@RequiredArgsConstructor
@Slf4j
public class DemoCredentialsInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void run(ApplicationArguments args) {
        try {
            userRepository.findByUsername("customer").ifPresentOrElse(user -> {
                String newHash = passwordEncoder.encode("COMP5348");
                user.setPasswordHash(newHash);
                user.setIsActive(true);
                userRepository.save(user);
                log.info("[DEMO_INIT] Updated demo user 'customer' password to COMP5348.");
            }, () -> {
                User demo = User.builder()
                        .username("customer")
                        .passwordHash(passwordEncoder.encode("COMP5348"))
                        .email("customer@store.com")
                        .firstName("Test")
                        .lastName("Customer")
                        .isActive(true)
                        .build();
                userRepository.save(demo);
                log.info("[DEMO_INIT] Created demo user 'customer' with password COMP5348.");
            });
        } catch (Exception e) {
            log.warn("[DEMO_INIT] Failed to ensure demo credentials: {}", e.getMessage());
        }
    }
}

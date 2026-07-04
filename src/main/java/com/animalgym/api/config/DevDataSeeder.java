package com.animalgym.api.config;

import com.animalgym.api.entity.User;
import com.animalgym.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevDataSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.findByEmail("admin@animalgym.com").isEmpty()) {
            User admin = User.builder()
                    .email("admin@animalgym.com")
                    .passwordHash(passwordEncoder.encode("secret123"))
                    .role("ADMIN")
                    .build();
            userRepository.save(admin);
        }
    }
}

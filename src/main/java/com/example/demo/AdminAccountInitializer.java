
package com.example.demo;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminAccountInitializer {

    @Bean
    public CommandLineRunner initializeAdmin(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${REVIEWME_ADMIN_USERNAME:admin}") String username,
            @Value("${REVIEWME_ADMIN_PASSWORD}") String password) {

        return args -> {
            User admin = userRepository.findByUsername(username)
                    .orElseGet(() -> {
                        User newAdmin = new User();
                        newAdmin.setUsername(username);
                        newAdmin.setPassword(passwordEncoder.encode(password));
                        return newAdmin;
                    });

            admin.setRole("ADMIN");
            userRepository.save(admin);

            System.out.println("ReviewMe administrator account initialized: " + username);
        };
    }
}

package org.example.tourism.config;

import org.example.tourism.common.Role;
import org.example.tourism.security.User;
import org.example.tourism.security.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
public class LoadDatabase {

    @Bean
    @Profile("!test")
    public CommandLineRunner initData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Check if users already exist before inserting
            if (userRepository.count() == 0) {
                User admin = new User(
                        "admin",
                        passwordEncoder.encode("admin123"),
                        "admin@mail.com",
                        "System Administrator",
                        true,
                        Set.of(Role.ADMIN)
                );
                userRepository.save(admin);

                User manager = new User(
                        "manager",
                        passwordEncoder.encode("manager123"),
                        "manager@mail.com",
                        "Hotel Manager",
                        true,
                        Set.of(Role.HOTEL_MANAGER)
                );
                userRepository.save(manager);

                User guest = new User(
                        "guest",
                        passwordEncoder.encode("guest123"),
                        "guest@mail.com",
                        "Regular Guest",
                        true,
                        Set.of(Role.GUEST)
                );
                userRepository.save(guest);

                System.out.println("Default users created:");
                System.out.println("Admin: admin/admin123");
                System.out.println("Manager: manager/manager123");
                System.out.println("Guest: guest/guest123");
            } else {
                System.out.println("Users already exist. Skipping initialization.");
            }
        };
    }
}
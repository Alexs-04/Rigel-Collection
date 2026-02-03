package com.korebit.rigel.config;

import com.korebit.rigel.repository.ConsumerRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class DataInit implements CommandLineRunner {
    private final ConsumerRepository consumerRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInit(ConsumerRepository consumerRepository, PasswordEncoder passwordEncoder) {
        this.consumerRepository = consumerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String @NonNull ... args) throws Exception {
        String defaultEmail = "admin";
        String defaultPassword = "admin";
        if (Objects.requireNonNull(consumerRepository.findByEmail(defaultEmail)).getEmail().equals(defaultEmail)) {
            var consumer = new com.korebit.rigel.model.Consumer();
            consumer.setEmail(defaultEmail);
            consumer.setPassword(passwordEncoder.encode(defaultPassword));
            consumer.setRole(com.korebit.rigel.model.enums.Role.ADMIN);
            consumerRepository.save(consumer);
        }
    }
}
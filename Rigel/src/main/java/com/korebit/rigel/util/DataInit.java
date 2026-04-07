package com.korebit.rigel.util;

import com.korebit.rigel.model.beans.Consumer;
import com.korebit.rigel.enums.Role;
import com.korebit.rigel.repository.ConsumerRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInit implements CommandLineRunner {
    private final ConsumerRepository consumerRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInit(ConsumerRepository consumerRepository, PasswordEncoder passwordEncoder) {
        this.consumerRepository = consumerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String @NonNull ... args) throws IllegalAccessError {
        String defaultEmail = "arlix@korebit";
        String defaultPassword = "root";
        var existingConsumer = consumerRepository.findByEmail(defaultEmail);
        if (existingConsumer == null) {
            var consumer = new Consumer();
            consumer.setName("Root");
            consumer.setUsername("root");
            consumer.setEmail(defaultEmail);
            consumer.setPassword(passwordEncoder.encode(defaultPassword));
            consumer.setRole(Role.ROOT);
            consumer.setActive(true);
            consumerRepository.save(consumer);
        }else {
            System.out.println("Default admin user already exists. Skipping creation.");
        }
    }
}
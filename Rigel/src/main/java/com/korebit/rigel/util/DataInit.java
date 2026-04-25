package com.korebit.rigel.util;

import com.korebit.rigel.model.beans.Consumer;
import com.korebit.rigel.enums.Role;
import com.korebit.rigel.repository.ConsumerRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds baseline application data during startup.
 *
 * <p>This initializer ensures a default root user exists for initial access.
 * If the default account is already present, no changes are applied.</p>
 */
@Component
public class DataInit implements CommandLineRunner {
    private final ConsumerRepository consumerRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInit(ConsumerRepository consumerRepository, PasswordEncoder passwordEncoder) {
        this.consumerRepository = consumerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Creates the default root account when it does not exist.
     *
     * <p>Default bootstrap values:</p>
     * <ul>
     *   <li>Email: {@code arlix@korebit}</li>
     *   <li>Username: {@code root}</li>
     *   <li>Password: {@code root} (stored encoded)</li>
     *   <li>Role: {@code ROOT}</li>
     * </ul>
     *
     * @param args command-line arguments (unused)
     * @throws IllegalAccessError if startup initialization cannot complete
     */
    @Override
    public void run(String @NonNull ... args) throws IllegalAccessError {
        String defaultEmail = "root@root";
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
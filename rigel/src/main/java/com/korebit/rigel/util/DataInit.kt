package com.korebit.rigel.util

import com.korebit.rigel.enums.Role
import com.korebit.rigel.model.beans.Consumer
import com.korebit.rigel.repository.ConsumerRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

/**
 * Seeds baseline application data during startup.
 *
 * This initializer ensures a default root user exists for initial access.
 * If the default account is already present, no changes are applied.
 */
@Component
class DataInit(
    private val consumerRepository: ConsumerRepository,
    private val passwordEncoder: PasswordEncoder,
) : CommandLineRunner {

    /**
     * Creates the default root account when it does not exist.
     *
     * Default bootstrap values:
     * - Email: `root@root`
     * - Username: `root`
     * - Password: `root` (stored encoded)
     * - Role: `ROOT`
     *
     * @param args command-line arguments (unused)
     */
    override fun run(vararg args: String) {
        val defaultEmail = "root@root"
        val defaultPassword = "root"
        if (consumerRepository.findByEmail(defaultEmail) == null) {
            consumerRepository.save(
                Consumer(
                    name = "Root",
                    username = "root",
                    email = defaultEmail,
                    password = passwordEncoder.encode(defaultPassword),
                    role = Role.ROOT,
                    active = true,
                )
            )
        } else {
            println("Default admin user already exists. Skipping creation.")
        }
    }
}

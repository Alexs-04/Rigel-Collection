package com.korebit.rigel.config

import com.korebit.rigel.repository.ConsumerRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class AppConfig(
    private val consumerRepository: ConsumerRepository,
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager =
        config.authenticationManager

    /**
     * Loads users by email. Invited users that have not activated their account yet have no
     * password: they are mapped with an empty one (never matches a BCrypt hash) and disabled,
     * instead of making [User] throw and turning a login attempt into a 500.
     */
    @Bean
    fun userDetailsService(): UserDetailsService = UserDetailsService { email ->
        val consumer = consumerRepository.findByEmail(email) ?: throw UsernameNotFoundException(email)
        val password = consumer.password
        User.builder()
            .username(consumer.email)
            .password(password ?: "")
            .authorities(listOf(SimpleGrantedAuthority("ROLE_${consumer.role.name}")))
            .disabled(!consumer.active || password == null)
            .build()
    }

    @Bean
    fun authenticationProvider(
        userDetailsService: UserDetailsService,
        passwordEncoder: PasswordEncoder,
    ): AuthenticationProvider {
        val provider = DaoAuthenticationProvider(userDetailsService)
        provider.setPasswordEncoder(passwordEncoder)
        return provider
    }
}

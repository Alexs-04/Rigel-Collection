package com.korebit.rigel.config;

import com.korebit.rigel.model.beans.Consumer;
import com.korebit.rigel.repository.ConsumerRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

/**
 * Spring configuration for authentication and security-related beans.
 */
@Configuration
public class AppConfig {

    private final ConsumerRepository consumerRepository;

    public AppConfig(ConsumerRepository consumerRepository) {
        this.consumerRepository = consumerRepository;
    }

    /**
     * Provides the password encoder used to hash and verify credentials.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Exposes the {@link AuthenticationManager} from the Spring configuration.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
        return config.getAuthenticationManager();
    }

    /**
     * Loads user details from the {@link ConsumerRepository} by email.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return email -> {
            final Consumer consumer = consumerRepository.findByEmail(email);
            if (consumer == null) {
                throw new UsernameNotFoundException(email);
            }
            return User.builder()
                    .username(consumer.getEmail())
                    .password(consumer.getPassword())
                    .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + consumer.getRole().name())))
                    .disabled(!consumer.getActive())
                    .build();
        };
    }

    /**
     * Configures the DAO authentication provider with the user details service and encoder.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(userDetailsService());
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }
}

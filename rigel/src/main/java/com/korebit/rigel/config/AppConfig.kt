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

@Configuration
public class AppConfig {

    private final ConsumerRepository consumerRepository;

    public AppConfig(ConsumerRepository consumerRepository) {
        this.consumerRepository = consumerRepository;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
        return config.getAuthenticationManager();
    }

    /**
     * Loads users by email. Invited users that have not activated their account yet have no
     * password: they are mapped with an empty one (never matches a BCrypt hash) and disabled,
     * instead of making {@link User} throw and turning a login attempt into a 500.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return email -> {
            final Consumer consumer = consumerRepository.findByEmail(email);
            if (consumer == null) {
                throw new UsernameNotFoundException(email);
            }
            final String password = consumer.getPassword();
            return User.builder()
                    .username(consumer.getEmail())
                    .password(password != null ? password : "")
                    .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + consumer.getRole().name())))
                    .disabled(!consumer.getActive() || password == null)
                    .build();
        };
    }

    @Bean
    public AuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }
}

package com.korebit.rigel.config;

import com.korebit.rigel.filter.RequestCorrelationFilter;
import com.korebit.rigel.util.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationProvider authenticationProvider,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            RequestCorrelationFilter requestCorrelationFilter
    ) {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authenticationProvider(authenticationProvider)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/consumer/api/add").permitAll()
                        .requestMatchers(HttpMethod.GET, "/product/**", "/suppliers/**")
                        .hasAnyRole("ROOT", "ADMIN", "USER", "SUPPLIER")
                        .requestMatchers(HttpMethod.POST, "/product/add", "/suppliers/add")
                        .hasAnyRole("ROOT", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/product/**", "/suppliers/**")
                        .hasAnyRole("ROOT", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/product/**", "/suppliers/**")
                        .hasAnyRole("ROOT", "ADMIN")
                        .requestMatchers("/consumer/**")
                        .hasAnyRole("ROOT", "ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(requestCorrelationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(jwtAuthenticationFilter, RequestCorrelationFilter.class);

        return http.build();
    }
}

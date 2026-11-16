package com.korebit.rigel.config;

import com.korebit.rigel.filter.RequestCorrelationFilter;
import com.korebit.rigel.util.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Security errors raised inside the filter chain (missing/invalid token, insufficient role)
     * are delegated to the MVC exception resolver so they reach
     * {@link com.korebit.rigel.exception.GlobalExceptionHandler} and share the same JSON error body
     * as every other API error: {@code 401} when unauthenticated, {@code 403} when forbidden.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationProvider authenticationProvider,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            RequestCorrelationFilter requestCorrelationFilter,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver
    ) {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .authenticationProvider(authenticationProvider)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                exceptionResolver.resolveException(request, response, null, authException))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                exceptionResolver.resolveException(request, response, null, accessDeniedException))
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/account/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
                        .requestMatchers("/admin/**").hasAnyRole("ADMIN", "ROOT")
                        .requestMatchers(HttpMethod.GET, "/product/**", "/suppliers/**", "/batches/**", "/purchases/**")
                        .hasAnyRole("ROOT", "ADMIN", "USER", "SUPPLIER")
                        .requestMatchers(HttpMethod.GET, "/amounts/**")
                        .hasAnyRole("ROOT", "ADMIN", "USER")
                        .requestMatchers(HttpMethod.POST, "/product/**", "/suppliers/**", "/batches/**", "/purchases/**")
                        .hasAnyRole("ROOT", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/amounts/**")
                        .hasAnyRole("ROOT", "ADMIN", "USER")
                        .requestMatchers(HttpMethod.PUT, "/product/**", "/suppliers/**", "/batches/**", "/purchases/**")
                        .hasAnyRole("ROOT", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/product/**")
                        .hasAnyRole("ROOT", "ADMIN", "USER")
                        .requestMatchers(HttpMethod.PUT, "/amounts/**")
                        .hasAnyRole("ROOT", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/product/**", "/suppliers/**", "/batches/**", "/purchases/**")
                        .hasAnyRole("ROOT", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/amounts/**")
                        .hasAnyRole("ROOT", "ADMIN", "USER")
                        .requestMatchers(HttpMethod.DELETE, "/amounts/**")
                        .hasAnyRole("ROOT", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/tickets", "/tickets/**")
                        .hasAnyRole("ROOT", "ADMIN", "USER")
                        .requestMatchers(HttpMethod.POST, "/tickets", "/tickets/**")
                        .hasAnyRole("ROOT", "ADMIN", "USER")
                        .requestMatchers(HttpMethod.DELETE, "/tickets", "/tickets/**")
                        .hasAnyRole("ROOT", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/logs", "/logs/**")
                        .hasAnyRole("ROOT", "ADMIN")
                        .requestMatchers("/consumer/api/users", "/consumer/api/users/**")
                        .hasRole("ROOT")
                        .requestMatchers("/consumer/**")
                        .hasAnyRole("ROOT", "ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(requestCorrelationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(jwtAuthenticationFilter, RequestCorrelationFilter.class);

        return http.build();
    }
}

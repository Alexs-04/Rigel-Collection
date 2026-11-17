package com.korebit.rigel.util;

import com.korebit.rigel.enums.TokenType;
import com.korebit.rigel.service.SessionTokenService;
import com.korebit.rigel.service.jwt.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Stateless JWT authentication filter.
 *
 * <p>A request is authenticated only when <b>all</b> of these hold:</p>
 * <ol>
 *   <li>the {@code Authorization} header carries a {@code Bearer} token with a valid signature that is not expired;</li>
 *   <li>the token is an <em>access</em> token (refresh tokens are rejected here);</li>
 *   <li>its {@code jti} is persisted, not revoked and not expired;</li>
 *   <li>the user still exists and is enabled.</li>
 * </ol>
 *
 * <p>Otherwise the request continues anonymously and Spring Security answers {@code 401}
 * for protected endpoints. Requests under {@code /auth/**} are skipped.</p>
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final SessionTokenService sessionTokenService;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   UserDetailsService userDetailsService,
                                   SessionTokenService sessionTokenService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.sessionTokenService = sessionTokenService;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return request.getServletPath().startsWith("/auth/");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            authenticate(request);
        }
        filterChain.doFilter(request, response);
    }

    private void authenticate(HttpServletRequest request) {
        final String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            return;
        }

        final Claims claims;
        try {
            claims = jwtService.parseClaims(header.substring(BEARER_PREFIX.length()).trim());
        } catch (JwtException | IllegalArgumentException invalidToken) {
            return;
        }

        if (jwtService.extractTokenType(claims) != TokenType.BEARER) {
            return;
        }
        final String username = claims.getSubject();
        if (username == null || !sessionTokenService.isActive(claims.getId(), TokenType.BEARER)) {
            return;
        }

        final UserDetails userDetails;
        try {
            userDetails = userDetailsService.loadUserByUsername(username);
        } catch (UsernameNotFoundException unknownUser) {
            return;
        }
        if (!userDetails.isEnabled() || !userDetails.isAccountNonLocked() || !userDetails.isAccountNonExpired()) {
            return;
        }

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}

package com.korebit.rigel.util;

import com.korebit.rigel.model.beans.Token;
import com.korebit.rigel.repository.TokenRepository;
import com.korebit.rigel.service.jwt.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

/**
 * Stateless JWT authentication filter.
 *
 * <p>This filter reads the {@code Authorization} header, validates bearer tokens,
 * verifies the token state persisted in the database, and populates the
 * {@link SecurityContextHolder} when authentication is successful.</p>
 *
 * <p>Requests under {@code /auth/**} and {@code /consumer/api/add} are excluded
 * to allow public authentication and registration flows.</p>
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenRepository tokenRepository;

    /**
     * Creates a filter that validates JWTs and loads user authentication data.
     *
     * @param jwtService service used to parse and validate JWT values
     * @param userDetailsService service used to load Spring Security user details
     * @param tokenRepository repository used to verify persisted token status (expired/revoked)
     */
    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService, TokenRepository tokenRepository) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.tokenRepository = tokenRepository;
    }

    /**
     * Defines endpoints that must bypass JWT authentication.
     *
     * @param request current HTTP request
     * @return {@code true} when the request targets a public endpoint
     */
    @Override
    protected boolean shouldNotFilter(@org.jspecify.annotations.NonNull HttpServletRequest request) {
        Objects.requireNonNull(request, "Request must not be null");
        String servletPath = request.getServletPath();
        return servletPath.startsWith("/auth/") || servletPath.equals("/consumer/api/add");
    }

    /**
     * Attempts to authenticate the request using a bearer JWT.
     *
     * <p>If the header is absent/invalid, the JWT cannot be parsed, the user cannot be loaded,
     * or the token is revoked/expired, the request continues without authentication.</p>
     *
     * @param request current HTTP request
     * @param response current HTTP response
     * @param filterChain remaining filter chain
     * @throws ServletException if filter execution fails
     * @throws IOException if I/O fails while delegating to the chain
     */
    @Override
    protected void doFilterInternal(
            @org.jspecify.annotations.NonNull HttpServletRequest request,
            @org.jspecify.annotations.NonNull HttpServletResponse response,
            @org.jspecify.annotations.NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        Objects.requireNonNull(request, "Request must not be null");
        Objects.requireNonNull(response, "Response must not be null");
        Objects.requireNonNull(filterChain, "Chain must not be null");
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        final String username;
        try {
            username = jwtService.extractUsername(jwt);
        } catch (RuntimeException ex) {
            filterChain.doFilter(request, response);
            return;
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails;
            try {
                userDetails = userDetailsService.loadUserByUsername(username);
            } catch (RuntimeException ex) {
                filterChain.doFilter(request, response);
                return;
            }
            Token storedToken = tokenRepository.findByToken(jwt);
            boolean tokenIsActive = storedToken != null && !storedToken.getExpired() && !storedToken.getRevoked();

            if (tokenIsActive && jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}

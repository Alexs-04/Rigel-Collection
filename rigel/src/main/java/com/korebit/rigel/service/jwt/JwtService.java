package com.korebit.rigel.service.jwt;

import com.korebit.rigel.model.beans.Consumer;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Provides JWT generation and validation utilities for authentication workflows.
 */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKet;
    @Value("${jwt.expiration}")
    private long jwtExpiration;
    @Value("${jwt.refresh}")
    private long jwtRefresh;

    /**
     * Generates an access token for the provided consumer.
     *
     * @param consumer authenticated consumer.
     * @return signed JWT access token.
     */
    public String generateToken(final Consumer consumer) {
        return buildToken(consumer, jwtExpiration);
    }

    /**
     * Generates a refresh token for the provided consumer.
     *
     * @param consumer authenticated consumer.
     * @return signed JWT refresh token.
     */
    public String generateRefreshToken(final Consumer consumer) {
        return buildToken(consumer, jwtRefresh);
    }

    /**
     * Extracts the username (token subject) from a token.
     *
     * @param token signed JWT token.
     * @return token subject.
     */
    public String extractUsername(final String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Validates that a token belongs to a user and has not expired.
     *
     * @param token signed JWT token.
     * @param userDetails user details to compare against.
     * @return true when token is valid for the provided user.
     */
    public boolean isTokenValid(final String token, final UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /**
     * Extracts a claim from a token using the given resolver function.
     *
     * @param token signed JWT token.
     * @param claimResolver resolver function for claim extraction.
     * @param <T> resolved claim type.
     * @return extracted claim value.
     */
    public <T> T extractClaim(final String token, final Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    private String buildToken(final Consumer consumer, final long expiration) {
        return Jwts.builder()
                .id(Objects.requireNonNull(consumer.getId()).toString())
                .claims(Map.of(
                        "name", consumer.getName(),
                        "role", consumer.getRole().name(),
                        "active", consumer.getActive()
                ))
                .subject(consumer.getEmail())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSecretKey(), Jwts.SIG.HS256) // ahora se especifica algoritmo
                .compact();
    }
    private boolean isTokenExpired(final String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private Claims extractAllClaims(final String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())   // antes setSigningKey
                .build()
                .parseSignedClaims(token)     // antes parseClaimsJws
                .getPayload();                // antes getBody
    }

    private SecretKey getSecretKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKet);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

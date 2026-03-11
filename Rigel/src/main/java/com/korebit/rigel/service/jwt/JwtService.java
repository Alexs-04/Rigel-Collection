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

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKet;
    @Value("${jwt.expiration}")
    private long jwtExpiration;
    @Value("${jwt.refresh}")
    private long jwtRefresh;

    public String generateToken(final Consumer consumer) {
        return buildToken(consumer, jwtExpiration);
    }

    public String generateRefreshToken(final Consumer consumer) {
        return buildToken(consumer, jwtRefresh);
    }

    public String extractUsername(final String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(final String token, final UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

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

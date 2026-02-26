package com.korebit.rigel.service.jwt;

import com.korebit.rigel.model.beans.Consumer;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

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

    private String buildToken(final Consumer consumer, final long expiration) {
        return Jwts.builder()
                .setId(Objects.requireNonNull(consumer.getId()).toString())
                .addClaims(Map.of("name", consumer.getName()))
                .setSubject(consumer.getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSecretKey())
                .compact();
    }

    private SecretKey getSecretKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKet);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

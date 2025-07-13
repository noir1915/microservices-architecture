package org.example.authservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    @Value("${api.secret}")
    private String secret;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        if (secret == null) {
            throw new IllegalStateException("JWT secret is not configured");
        }
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }


    public String generateToken(String username) {
        long nowMillis = System.currentTimeMillis();
        long expMillis = nowMillis + 1000 * 60 * 60 * 10; // 10 часов
        Date now = new Date(nowMillis);
        Date expiryDate = new Date(expMillis);

        return Jwts.builder()
                .setSubject(String.valueOf(username))
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUsername(String token) {
        try {
            Claims claims = getClaims(token);
            return claims.getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    public boolean validateToken(String token, String username) {
        try {
            Claims claims = getClaims(token);
            String tokenUsername = claims.getSubject();
            Date expiration = claims.getExpiration();

            return (tokenUsername.equals(username) && !expiration.before(new Date()));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}

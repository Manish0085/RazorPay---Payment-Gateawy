package com.razorpay.payment_gateway.merchant.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private int tokenExpiry;

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String generateAccessToken(String email, UUID merchantId, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(email)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(tokenExpiry)))
                .claim("merchant_id", merchantId)
                .claim("role", role)
                .signWith(getSecretKey())
                .compact();
    }

    public Claims verifyAccessToken(String accessToken) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(accessToken)
                .getPayload();
    }


    public String extractRole(Claims claims) {
        return claims.get("role", String.class);
    }

    public String extractMerchantId(Claims claims) {
        return claims.get("merchant_id", String.class);
    }
}

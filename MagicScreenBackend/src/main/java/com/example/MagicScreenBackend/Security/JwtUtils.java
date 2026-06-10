package com.example.MagicScreenBackend.Security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {

    // A secure 256-bit secret key generated safely for signing tokens
    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // Token validity window: 24 Hours (in milliseconds)
    private final int jwtExpirationMs = 86400000;

    // Generate token based on authenticated admin user details
    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key)
                .compact();
    }

    // Extract username out from a raw JWT string
    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    // Validate if the token is structurally sound and not expired
    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // Token is invalid, tampered with, or expired
            return false;
        }
    }
}
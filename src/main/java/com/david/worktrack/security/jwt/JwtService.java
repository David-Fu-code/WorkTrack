package com.david.worktrack.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    // Secret key used to sign and verify JWT
    @Value("${jwt.secret}")
    private String SECRET_KEY;

    // Token expiration time (in milliseconds)
    @Value("${jwt.expiration}")
    private long EXPIRATION_TIME;

    // Converts the SECRET KEY into a Key object → ready to sign or verify JWT.
    private Key getSingKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    // Generate token for a username (email)
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username) // Store username
                .setIssuedAt(new Date(System.currentTimeMillis())) // When token is created
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // expires in 4h
                .signWith(getSingKey(), SignatureAlgorithm.HS256) // Signs the token -> makes it tamper-proof -> cannot be modified
                .compact();
    }

    // Extract username (subject) from token
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    // Validate token : correct user and not expired
    public boolean isTokenValid(String token, String username) {
        return username.equals(extractUsername(token)) && !isTokenExpired(token);
    }

    // Check if token is expired
    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    // Extract all claims from token (centralized parsing logic)
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSingKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }


}

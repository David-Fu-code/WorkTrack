package com.david.worktrack.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {
    // SECRET KEY (used to sign and verify the JWT)-> should be 256-bit (32+ characters)
    private final static String  SECRET_KEY = "your-256-bit-secret-your-256-bit-secret-your-256-bit-secret";

    // Converts the SECRET KEY into a Key object → ready to sign or verify JWT.
    private Key getSingKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    // Generate token for a username (email)
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username) // Store username
                .setIssuedAt(new Date(System.currentTimeMillis())) // When token is created
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // expires in 24h
                .signWith(getSingKey(), SignatureAlgorithm.HS256) // Signs the token -> makes it tamper-proof -> cannot be modified
                .compact();
    }

    // JWS → signed JWT
    // JWT → generic token format, can be signed (JWS) or not signed (rare), or encrypted (JWE)
    // Extract username from token -> Reads the username (subject) from the token.
    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSingKey())
                .build()
                .parseClaimsJws(token) // This case is ClaimsJws(signed token) and not ClaimsJwt
                .getBody()
                .getSubject();
    }

    // Validate token -> is username correct AND token not expired
    /* We check:
    Is token signed correctly?
    Is token not expired?
    Is token really for this user?
    */
    public boolean isTokenValid(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username)) && !isTokenExpired(token);
    }

    // Check if token is expired
    private boolean isTokenExpired(String token) {
        Date expiration = Jwts.parserBuilder()
                .setSigningKey(getSingKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();

        return expiration.before(new Date());
    }


}

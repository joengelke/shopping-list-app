package com.joengelke.shoppinglistapp.backend.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    @Value("${security.jwt-secret}")
    private String secretKey;
    @Value("${security.expiration-time}")
    private long expirationTime;


    public String generateToken(Authentication authentication, String userId) {
        String username = authentication.getName();
        String authorities = authentication.getAuthorities().stream(). // roles in here
                map(GrantedAuthority::getAuthority).
                collect(Collectors.joining(","));
        Instant currentDate = Instant.now();
        Instant expireDate = currentDate.plusMillis(expirationTime); // default 1000 days

        // return token
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("authorities", authorities)
                .setIssuedAt(Date.from(currentDate))
                .setExpiration(Date.from(expireDate))
                .signWith(getSignInKey(),SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            throw new AuthenticationCredentialsNotFoundException("JWT was expired or incorrect", e.fillInStackTrace());
        }
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public String getUserIdFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody().get("userId", String.class);
    }


    public boolean isAdmin(String token) {
        String authorities = Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("authorities", String.class);

        return authorities != null && Arrays.asList(authorities.split(","))
                .contains("ROLE_ADMIN");
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

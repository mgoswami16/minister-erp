package com.ministererp.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    private Key signingKey;

    // ── Initialize signing key on startup
    @PostConstruct
    public void init() {
        byte[] keyBytes = Base64.getEncoder()
                .encode(jwtSecret.getBytes());
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    // ── Generate token from Authentication
    public String generateToken(Authentication authentication) {
        UserDetails userDetails =
                (UserDetails) authentication.getPrincipal();
        return generateToken(userDetails);
    }

    // ── Generate token from UserDetails
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        // Add role to claims
        if (!userDetails.getAuthorities().isEmpty()) {
            claims.put("role",
                    userDetails.getAuthorities()
                               .iterator()
                               .next()
                               .getAuthority()
                               .replace("ROLE_", ""));
        }

        return buildToken(claims,
                userDetails.getUsername(),
                jwtExpiration);
    }

    // ── Generate token with extra claims
    public String generateTokenWithClaims(
            String username,
            String role,
            String userId,
            String name) {

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("userId", userId);
        claims.put("name", name);

        return buildToken(claims, username, jwtExpiration);
    }

    // ── Build JWT token
    private String buildToken(
            Map<String, Object> extraClaims,
            String subject,
            long expiration) {

        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(subject)
                .setIssuedAt(new Date(
                        System.currentTimeMillis()))
                .setExpiration(new Date(
                        System.currentTimeMillis() + expiration))
                .signWith(signingKey,
                        SignatureAlgorithm.HS256)
                .compact();
    }

    // ── Extract username from token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // ── Extract role from token
    public String extractRole(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("role", String.class);
    }

    // ── Extract userId from token
    public String extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("userId", String.class);
    }

    // ── Extract name from token
    public String extractName(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("name", String.class);
    }

    // ── Extract expiration
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // ── Extract any claim
    public <T> T extractClaim(
            String token,
            Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // ── Extract all claims
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // ── Validate token
    public boolean validateToken(
            String token,
            UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(
                userDetails.getUsername()))
                && !isTokenExpired(token);
    }

    // ── Check if token is expired
    private boolean isTokenExpired(String token) {
        return extractExpiration(token)
                .before(new Date());
    }

    // ── Validate token format only
    public boolean isValidToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (MalformedJwtException e) {
            System.out.println("Invalid JWT token");
        } catch (ExpiredJwtException e) {
            System.out.println("JWT token expired");
        } catch (UnsupportedJwtException e) {
            System.out.println("JWT token unsupported");
        } catch (IllegalArgumentException e) {
            System.out.println("JWT claims string empty");
        }
        return false;
    }

    // ── Get expiration time in ms
    public long getExpirationTime() {
        return jwtExpiration;
    }
}
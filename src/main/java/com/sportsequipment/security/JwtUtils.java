package com.sportsequipment.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    public static final String CLAIM_KEY_TYPE = "typ";
    public static final String TOKEN_TYPE_ACCESS = "access";
    public static final String TOKEN_TYPE_REFRESH = "refresh";

    @Value("${sportsequipment.app.jwtSecret}")
    private String jwtSecret;

    @Value("${sportsequipment.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    @Value("${sportsequipment.app.accessTokenExpirationMs}")
    private int accessTokenExpirationMs;

    @Value("${sportsequipment.app.refreshTokenExpirationMs}")
    private int refreshTokenExpirationMs;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateJwtToken(Authentication authentication) {
        return generateAccessToken(authentication);
    }

    public String generateAccessToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_KEY_TYPE, TOKEN_TYPE_ACCESS);
        return buildToken(userPrincipal.getUsername(), claims, accessTokenExpirationMs);
    }

    public String generateAccessTokenForUsername(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_KEY_TYPE, TOKEN_TYPE_ACCESS);
        return buildToken(username, claims, accessTokenExpirationMs);
    }

    public String generateRefreshToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_KEY_TYPE, TOKEN_TYPE_REFRESH);
        return buildToken(userPrincipal.getUsername(), claims, refreshTokenExpirationMs);
    }

    public String generateRefreshTokenForUsername(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_KEY_TYPE, TOKEN_TYPE_REFRESH);
        return buildToken(username, claims, refreshTokenExpirationMs);
    }

    private String buildToken(String subject, Map<String, Object> claims, int expirationMs) {
        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        return parseClaims(token).getBody().getSubject();
    }

    public String getJtiFromJwtToken(String token) {
        return parseClaims(token).getBody().getId();
    }

    public Date getExpirationFromJwtToken(String token) {
        return parseClaims(token).getBody().getExpiration();
    }

    public String getTokenTypeFromJwtToken(String token) {
        Object typ = parseClaims(token).getBody().get(CLAIM_KEY_TYPE);
        return typ == null ? TOKEN_TYPE_ACCESS : typ.toString();
    }

    public boolean isRefreshToken(String token) {
        return TOKEN_TYPE_REFRESH.equals(getTokenTypeFromJwtToken(token));
    }

    public boolean isAccessToken(String token) {
        Object typ = parseClaims(token).getBody().get(CLAIM_KEY_TYPE);
        // 兼容旧 token（没有 typ 字段时视为 access）
        return typ == null || TOKEN_TYPE_ACCESS.equals(typ.toString());
    }

    public int getAccessTokenExpirationMs() {
        return accessTokenExpirationMs;
    }

    public int getRefreshTokenExpirationMs() {
        return refreshTokenExpirationMs;
    }

    private Jws<Claims> parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
    }

    public boolean validateJwtToken(String authToken) {
        try {
            parseClaims(authToken);
            return true;
        } catch (io.jsonwebtoken.security.SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }
}

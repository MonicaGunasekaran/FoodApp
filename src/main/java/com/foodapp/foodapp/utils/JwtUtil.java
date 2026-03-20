package com.foodapp.foodapp.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

public final class JwtUtil {

    private static final String SECRET =
            "foodapp_super_secret_key_change_this_foodapp_256bit";

    private static final long EXPIRATION =
            1000 * 60 * 60;

    private static final Key KEY =
            Keys.hmacShaKeyFor(
                    SECRET.getBytes(StandardCharsets.UTF_8)
            );

    public static String generateToken(
            UUID userId,
            String role
    ) {

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + EXPIRATION)
                )
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }


    private static Claims parse(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public static String getUserId(String token) {
        return parse(token).getSubject();
    }

    public static String getRole(String token) {
        return parse(token).get("role", String.class);
    }

    public static boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}

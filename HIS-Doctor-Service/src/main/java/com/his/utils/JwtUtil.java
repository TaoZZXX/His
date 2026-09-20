package com.his.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {
    @Value("${jwt.secret:his@2026#jwtSecretKey1234567890}")
    private String secretKey;
    @Value("${jwt.expire.minutes:120}")
    private Integer expireMinutes;
    private SecretKey getSecretKey() { return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)); }
    public Claims parseToken(String token) { try { return Jwts.parserBuilder().setSigningKey(getSecretKey()).build().parseClaimsJws(token).getBody(); } catch (Exception e) { return null; } }
    public Long getUserIdFromToken(String token) { Claims c = parseToken(token); return c == null ? null : c.get("userId", Long.class); }
    public String generateToken(Long userId, String username) { Map<String, Object> claims = new HashMap<>(); claims.put("userId", userId); claims.put("username", username); return Jwts.builder().setClaims(claims).setSubject(username).setIssuedAt(new Date()).setExpiration(DateUtils.addMinutes(new Date(), expireMinutes)).signWith(getSecretKey(), SignatureAlgorithm.HS256).compact(); }
}

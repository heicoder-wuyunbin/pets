package com.pets.auth.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具：HS256 签发与解析。
 * Claims: sub=account_id, clerk_id, shop_id, chain_group_id, role_id, exp。
 */
@Component
public class JwtUtil {

    @Value("${pets.jwt.secret}")
    private String secret;

    @Value("${pets.jwt.expire-minutes:720}")
    private long expireMinutes;

    private SecretKey key;

    @PostConstruct
    void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Long accountId, Long clerkId, Long shopId, Long chainGroupId, Long roleId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expireMinutes * 60_000L);
        return Jwts.builder()
                .subject(String.valueOf(accountId))
                .claim("clerk_id", clerkId)
                .claim("shop_id", shopId)
                .claim("chain_group_id", chainGroupId)
                .claim("role_id", roleId)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getAccountId(String token) {
        return Long.valueOf(parseToken(token).getSubject());
    }
}

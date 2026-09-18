package com.campus.secondhand.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    // 签名密钥（请放在 application.yml 中，不要硬编码在代码里）
    private static final String SECRET_KEY = "YourSuperSecretKeyForJwtTokenGenerationMustBeLongEnough";
    
    // 生成 Token
    public String generateToken(Long userId, long expireTimeMillis) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expireTimeMillis);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .compact();
    }

    // 解析 Token 获取 Claims
    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }

    // 从 Token 中获取用户 ID
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return Long.valueOf(claims.getSubject());
    }

    // 校验 Token 是否有效
    public boolean validateToken(String token) {
        try {
            getClaimsFromToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String generateToken(Long id, String username) {
        return null;
    }
}
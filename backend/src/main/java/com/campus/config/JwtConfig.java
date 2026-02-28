package com.campus.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT工具类
 * 提供JWT令牌的生成、解析和验证功能
 */
@Component
public class JwtConfig {

    public static final String HEADER_PREFIX = "Bearer ";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.header}")
    private String header;

    @Value("${jwt.prefix}")
    private String prefix;

    /**
     * 获取签名密钥
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 从令牌中获取用户名
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * 从令牌中获取角色
     */
    public Integer getRoleFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        Object role = claims.get("role");
        if (role instanceof Integer) {
            return (Integer) role;
        } else if (role instanceof String) {
            return Integer.parseInt((String) role);
        }
        return null;
    }

    /**
     * 从令牌中获取过期时间
     */
    public Date getExpirationFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * 从令牌中获取指定声明
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 解析令牌获取所有声明
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 检查令牌是否过期
     */
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * 生成令牌
     */
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }

    /**
     * 创建令牌
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .header().empty().add("typ", "JWT")
                .and()
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 验证令牌
     */
    public Boolean validateToken(String token, String username) {
        final String tokenUsername = getUsernameFromToken(token);
        return (tokenUsername.equals(username) && !isTokenExpired(token));
    }

    /**
     * 刷新令牌
     */
    public String refreshToken(String token) {
        final Claims claims = getAllClaimsFromToken(token);
        claims.put("issuedAt", new Date());
        claims.put("expiration", new Date(System.currentTimeMillis() + expiration));
        return createToken(claims, claims.getSubject());
    }

    /**
     * 获取请求头名称
     */
    public String getHeader() {
        return header;
    }

    /**
     * 获取令牌前缀
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * 从请求头中提取Token
     * @param authHeader Authorization请求头的值
     * @return 提取出的token，不包含Bearer前缀；如果无效返回null
     */
    public String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith(HEADER_PREFIX)) {
            return null;
        }
        return authHeader.substring(HEADER_PREFIX.length());
    }

    /**
     * 从HttpServletRequest中提取Token
     * @param request HTTP请求对象
     * @return 提取出的token，不包含Bearer前缀；如果无效返回null
     */
    public String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader(header);
        return extractToken(authHeader);
    }
}

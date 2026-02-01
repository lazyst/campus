package com.campus.config;

import com.campus.modules.auth.service.AuthService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * JWT认证过滤器
 * 在Spring Security认证之前拦截请求，解析JWT token并设置认证信息
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtConfig jwtConfig;
    private final AuthService authService;
    private final SecretKey jwtSecretKey;

    public JwtAuthenticationFilter(JwtConfig jwtConfig, AuthService authService,
                                   @Value("${jwt.secret}") String jwtSecret) {
        this.jwtConfig = jwtConfig;
        this.authService = authService;
        this.jwtSecretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // 从请求头获取Authorization
        final String authHeader = request.getHeader("Authorization");

        // 如果没有Authorization header或者格式不对，直接放行让后续处理
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 提取token（去掉 "Bearer " 前缀）
            final String jwt = authHeader.substring(7);

            // 解析token获取claims
            Claims claims = Jwts.parser()
                    .verifyWith(jwtSecretKey)
                    .build()
                    .parseSignedClaims(jwt)
                    .getPayload();

            // 获取role信息
            Integer role = claims.get("role", Integer.class);

            // 确定权限列表
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

            // 如果是管理员(role=1)，添加管理员权限
            if (role != null && role == 1) {
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            }

            // 获取主题（可能是手机号或管理员ID）
            String subject = claims.getSubject();

            // 创建认证对象
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    subject,
                    null,
                    authorities
            );

            // 设置请求详情
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 将认证信息设置到SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authToken);

        } catch (Exception e) {
            // Token无效时不阻止请求，让后续的认证机制处理
            // Spring Security会返回401/403
            logger.debug("JWT token validation failed: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    // 导入需要的类
    private static class Keys {
        public static javax.crypto.SecretKey hmacShaKeyFor(byte[] keyBytes) {
            return new javax.crypto.spec.SecretKeySpec(keyBytes, "HmacSHA512");
        }
    }
}

package com.campus.config;

import com.campus.modules.auth.service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT认证过滤器
 * 在Spring Security认证之前拦截请求，解析JWT token并设置认证信息
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtConfig jwtConfig;
    private final AuthService authService;

    public JwtAuthenticationFilter(JwtConfig jwtConfig, AuthService authService) {
        this.jwtConfig = jwtConfig;
        this.authService = authService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        System.out.println("JJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJ 1 - " + request.getMethod() + " " + request.getRequestURI());

        // 从请求头获取Authorization
        final String authHeader = request.getHeader("Authorization");
        System.out.println("JJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJ 2 - authHeader: " + (authHeader != null ? authHeader.substring(0, Math.min(30, authHeader.length())) + "..." : "null"));

        // 如果没有Authorization header或者格式不对，直接放行让后续处理
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("JJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJ 3 - 无token，放行");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 提取token（去掉 "Bearer " 前缀）
            final String jwt = authHeader.substring(7);
            System.out.println("JJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJ 4 - jwt: " + jwt.substring(0, Math.min(30, jwt.length())) + "...");

            // 验证token并获取手机号
            boolean isValid = authService.validateToken(jwt);
            System.out.println("JJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJ 5 - validateToken: " + isValid);

            if (isValid) {
                final String phone = jwtConfig.getUsernameFromToken(jwt);
                System.out.println("JJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJ 6 - phone: " + phone);

                // 创建认证对象
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        phone,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                );

                // 设置请求详情
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 将认证信息设置到SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authToken);
                System.out.println("JJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJ 7 - 已设置认证信息");
            }
        } catch (Exception e) {
            // Token无效时不阻止请求，让后续的认证机制处理
            // Spring Security会返回401/403
            System.out.println("JJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJ 8 - JWT异常: " + e.getMessage());
            logger.debug("JWT token validation failed: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
        System.out.println("JJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJ 9 - filterChain完成");
    }
}

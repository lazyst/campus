package com.campus.modules.admin.service.impl;
import com.baomidou.dynamic.datasource.annotation.DS;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.common.Result;
import com.campus.modules.admin.entity.Admin;
import com.campus.modules.admin.mapper.AdminMapper;
import com.campus.modules.admin.service.AdminService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 管理员服务实现
 */
@Service
@DS("slave")
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private static final long EXPIRATION = 7 * 24 * 60 * 60 * 1000; // 7天
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public Admin getByUsername(String username) {
        LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Admin::getUsername, username)
               .eq(Admin::getDeleted, false);
        return this.getOne(wrapper);
    }

    @Override
    public String login(String username, String password) {
        Admin admin = getByUsername(username);
        
        if (admin == null) {
            throw new IllegalArgumentException("管理员不存在");
        }
        
        if (admin.getStatus() == 0) {
            throw new IllegalArgumentException("管理员已被禁用");
        }
        
        // 验证密码
        if (!passwordEncoder.matches(password, admin.getPassword())) {
            throw new IllegalArgumentException("密码错误");
        }
        
        // 生成Token
        String token = generateToken(admin);
        
        // 更新登录信息
        admin.setLastLoginTime(LocalDateTime.now());
        this.updateById(admin);
        
        return token;
    }

    @Override
    public boolean isSuperAdmin(Long adminId) {
        Admin admin = this.getById(adminId);
        return admin != null && admin.getRole() == 1;
    }

    @Override
    public Long getAdminIdFromToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String subject = claims.getSubject();
            return subject != null ? Long.parseLong(subject) : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 生成JWT Token
     */
    private String generateToken(Admin admin) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        
        Date now = new Date();
        Date expiration = new Date(now.getTime() + EXPIRATION);
        
        return Jwts.builder()
                .subject(admin.getId().toString())
                .claim("username", admin.getUsername())
                .claim("role", admin.getRole())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(key)
                .compact();
    }
}

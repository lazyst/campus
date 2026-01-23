# Spring Boot 3 + MyBatis-Plus REST API 开发最佳实践研究报告

## 1. 项目结构和架构模式

### 推荐的项目目录结构

基于 Spring Boot 官方推荐的架构模式，建议采用按功能模块划分的结构：

```
com
  +- example
      +- myapplication
          +- MyApplication.java          # 应用启动类（放在根包下）
          |
          +- common                       # 公共模块
          |   +- config                   # 配置类
          |   +- exception               # 全局异常处理
          |   +- result                  # 统一响应格式
          |   +- utils                   # 工具类
          |
          +- modules                      # 功能模块
              +- user                    # 用户模块
              |   +- entity              # 实体类
              |   +- mapper              # MyBatis-Plus Mapper接口
              |   +- service             # 服务层接口
              |   |   +- impl            # 服务层实现
              |   +- controller          # REST控制器
              |   +- dto                 # 数据传输对象
              |   +- vo                  # 视图对象
              |
              +- social                  # 社交功能模块
                  +- entity
                  +- mapper
                  +- service
                  |   +- impl
                  +- controller
```

**架构说明**：
- **Controller层**：处理HTTP请求，参数校验，调用Service层
- **Service层**：业务逻辑处理，事务管理
- **Mapper层**：数据访问，由MyBatis-Plus自动生成基本CRUD
- **Entity/DTO/VO**：清晰的数据边界划分

### 核心依赖配置

```xml
<!-- pom.xml 主要依赖 -->
<dependencies>
    <!-- Spring Boot Starters -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    
    <!-- MyBatis-Plus -->
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
        <version>3.5.5</version>
    </dependency>
    
    <!-- MySQL Driver -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>
    
    <!-- JWT -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt</artifactId>
        <version>0.12.3</version>
    </dependency>
    
    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    
    <!-- Swagger/OpenAPI -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>2.3.0</version>
    </dependency>
</dependencies>
```

## 2. 用户认证和授权（JWT）

### JWT认证流程

JWT（JSON Web Token）认证流程包括：
1. 用户提交用户名/密码
2. 服务端验证并生成JWT Token
3. 客户端保存Token（通常在localStorage）
4. 后续请求携带Token（Authorization header）
5. 服务端验证Token并提取用户信息

### 核心JWT工具类

```java
package com.example.common.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {
    
    @Value("${jwt.secret:your-256-bit-secret-key-for-jwt-signing-must-be-long-enough}")
    private String secret;
    
    @Value("${jwt.expiration:604800000}") // 7天有效期
    private long expiration;
    
    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * 生成Token
     */
    public String generateToken(Long userId, String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        
        return Jwts.builder()
                .subject(userId.toString())
                .claim("username", username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSecretKey())
                .compact();
    }
    
    /**
     * 解析Token
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    /**
     * 获取用户ID
     */
    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        return Long.parseLong(claims.getSubject());
    }
    
    /**
     * 获取用户名
     */
    public String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims.get("username", String.class);
    }
    
    /**
     * 验证Token是否有效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * 判断Token是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}
```

### JWT认证过滤器

```java
package com.example.common.security;

import com.example.common.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = extractJwtFromRequest(request);
            
            if (StringUtils.hasText(jwt) && jwtUtil.validateToken(jwt)) {
                Long userId = jwtUtil.getUserId(jwt);
                String username = jwtUtil.getUsername(jwt);
                
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        userDetails, 
                        null, 
                        userDetails.getAuthorities()
                    );
                
                authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
                );
                
                // 将用户ID存入request供后续使用
                request.setAttribute("userId", userId);
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
```

### Spring Security配置

```java
package com.example.common.config;

import com.example.common.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                // 公开API
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                // 其他请求需要认证
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
```

## 3. CRUD操作与MyBatis-Plus

### Mapper层基础CRUD

MyBatis-Plus通过继承`BaseMapper<T>`提供强大的CRUD能力：

```java
// UserMapper.java
package com.example.modules.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.modules.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 继承后自动拥有以下方法：
    // insert(T entity), deleteById(Serializable id), 
    // selectById(Serializable id), updateById(T entity), 
    // selectList(wrapper), selectPage(page, wrapper)等
}
```

### 实体类配置

```java
// User.java
package com.example.modules.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("users")
public class User {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String username;
    
    private String phone;
    
    private String email;
    
    private String password;
    
    private String nickname;
    
    private String gender;
    
    private String bio;
    
    private String avatar;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    @TableLogic  // 逻辑删除注解
    private Integer deleted;
}
```

### 条件构造器使用

**QueryWrapper示例**：

```java
// 基本条件查询
QueryWrapper<User> queryWrapper = new QueryWrapper<>();
queryWrapper.eq("phone", "13800138000")           // phone = '13800138000'
           .eq("status", 1)                        // status = 1
           .gt("age", 18)                          // age > 18
           .like("nickname", "张")                  // nickname LIKE '%张%'
           .orderByDesc("created_at");             // ORDER BY created_at DESC

List<User> users = userMapper.selectList(queryWrapper);

// LambdaQueryWrapper（类型安全，推荐）
LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
lambdaQueryWrapper.eq(User::getPhone, "13800138000")
                 .eq(User::getStatus, 1)
                 .gt(User::getAge, 18)
                 .like(User::getNickname, "张")
                 .orderByDesc(User::getCreatedAt);

List<User> users = userMapper.selectList(lambdaQueryWrapper);

// 复杂条件查询
LambdaQueryWrapper<User> complexQuery = new LambdaQueryWrapper<>();
complexQuery.and(wrapper -> wrapper
    .eq("status", 1)
    .or()
    .isNotNull("email")
);
```

### 分页查询

**配置分页拦截器**：

```java
// MyBatisPlusConfig.java
package com.example.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyBatisPlusConfig {
    
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 添加分页插件，指定数据库类型
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
```

**Service层分页查询**：

```java
// UserServiceImpl.java
package com.example.modules.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.modules.user.entity.User;
import com.example.modules.user.mapper.UserMapper;
import com.example.modules.user.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    
    @Override
    public IPage<User> getUserPage(int pageNum, int pageSize, String keyword) {
        Page<User> page = new Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like("username", keyword)
                   .or()
                   .like("nickname", keyword)
                   .orderByDesc(User::getCreatedAt);
        
        return this.page(page, queryWrapper);
    }
}
```

## 4. 数据库设计模式（社交功能）

### 核心社交功能表结构

#### 用户表（users）

```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL UNIQUE COMMENT '手机号',
    email VARCHAR(100) NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    nickname VARCHAR(50) NULL,
    gender TINYINT DEFAULT 0 COMMENT '0:未知 1:男 2:女',
    bio TEXT NULL COMMENT '个人简介',
    avatar VARCHAR(500) NULL COMMENT '头像URL',
    status TINYINT DEFAULT 1 COMMENT '1:正常 0:禁用 2:封禁',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    
    INDEX idx_phone (phone),
    INDEX idx_username (username),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

#### 板块表（boards）

```sql
CREATE TABLE boards (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '板块名称',
    description VARCHAR(200) NULL COMMENT '板块描述',
    icon VARCHAR(100) NULL COMMENT '板块图标',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '1:显示 0:隐藏',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛板块表';
```

#### 帖子表（posts）

```sql
CREATE TABLE posts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '发布者ID',
    board_id BIGINT NOT NULL COMMENT '板块ID',
    title VARCHAR(100) NULL COMMENT '帖子标题',
    content TEXT NOT NULL COMMENT '帖子内容',
    images JSON NULL COMMENT '图片URL数组',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    comment_count INT DEFAULT 0 COMMENT '评论数',
    view_count INT DEFAULT 0 COMMENT '浏览数',
    status TINYINT DEFAULT 1 COMMENT '1:正常 0:已删除',
    top TINYINT DEFAULT 0 COMMENT '是否置顶',
    essence TINYINT DEFAULT 0 COMMENT '是否精华',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (board_id) REFERENCES boards(id) ON DELETE CASCADE,
    
    INDEX idx_user_id (user_id),
    INDEX idx_board_id (board_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子表';
```

#### 评论表（comments）

```sql
CREATE TABLE comments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '评论者ID',
    post_id BIGINT NOT NULL COMMENT '帖子ID',
    parent_comment_id BIGINT NULL COMMENT '父评论ID',
    content TEXT NOT NULL COMMENT '评论内容',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    status TINYINT DEFAULT 1 COMMENT '1:正常 0:已删除',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_comment_id) REFERENCES comments(id) ON DELETE CASCADE,
    
    INDEX idx_post_id (post_id),
    INDEX idx_user_id (user_id),
    INDEX idx_parent_id (parent_comment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';
```

#### 点赞表（likes）

```sql
CREATE TABLE likes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '点赞用户ID',
    post_id BIGINT NOT NULL COMMENT '帖子ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    
    UNIQUE KEY uk_user_post (user_id, post_id),
    INDEX idx_post_id (post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞表';
```

#### 收藏表（collects）

```sql
CREATE TABLE collects (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '收藏用户ID',
    post_id BIGINT NOT NULL COMMENT '帖子ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    
    UNIQUE KEY uk_user_post (user_id, post_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';
```

#### 闲置物品表（items）

```sql
CREATE TABLE items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '发布者ID',
    title VARCHAR(100) NOT NULL COMMENT '物品标题',
    description TEXT NULL COMMENT '物品描述',
    price DECIMAL(10,2) NOT NULL COMMENT '价格',
    type TINYINT NOT NULL COMMENT '1:出售 2:求购',
    images JSON NULL COMMENT '图片URL数组',
    status TINYINT DEFAULT 1 COMMENT '1:正常 2:已完成 3:已下架 0:已删除',
    view_count INT DEFAULT 0 COMMENT '浏览数',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    INDEX idx_user_id (user_id),
    INDEX idx_type (type),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='闲置物品表';
```

#### 对话表（conversations）

```sql
CREATE TABLE conversations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user1_id BIGINT NOT NULL COMMENT '用户1',
    user2_id BIGINT NOT NULL COMMENT '用户2',
    last_message_id BIGINT NULL COMMENT '最后消息ID',
    last_message_at TIMESTAMP NULL COMMENT '最后消息时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user1_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (user2_id) REFERENCES users(id) ON DELETE CASCADE,
    
    UNIQUE KEY uk_users (user1_id, user2_id),
    INDEX idx_user1 (user1_id),
    INDEX idx_user2 (user2_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对话表';
```

#### 消息表（messages）

```sql
CREATE TABLE messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    conversation_id BIGINT NOT NULL COMMENT '对话ID',
    sender_id BIGINT NOT NULL COMMENT '发送者ID',
    receiver_id BIGINT NOT NULL COMMENT '接收者ID',
    content TEXT NOT NULL COMMENT '消息内容',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read TINYINT DEFAULT 0 COMMENT '0:未读 1:已读',
    
    FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE,
    
    INDEX idx_conversation (conversation_id),
    INDEX idx_sender (sender_id),
    INDEX idx_receiver (receiver_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';
```

## 5. RESTful API设计

### 统一响应格式

```java
// Result.java
package com.example.common.result;

import lombok.Data;
import java.io.Serializable;

@Data
public class Result<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int code;
    private String message;
    private T data;
    private long timestamp;
    
    public Result() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }
    
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }
    
    public static <T> Result<T> success() {
        return new Result<>(200, "success", null);
    }
    
    public static <T> Result<T> success(int code, String message, T data) {
        return new Result<>(code, message, data);
    }
    
    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null);
    }
    
    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }
    
    public static <T> Result<T> error(int code, String message, T data) {
        return new Result<>(code, message, data);
    }
}
```

### Controller基础结构

```java
// AuthController.java
package com.example.modules.auth.controller;

import com.example.common.result.Result;
import com.example.modules.auth.dto.LoginRequest;
import com.example.modules.auth.dto.RegisterRequest;
import com.example.modules.auth.service.AuthService;
import com.example.modules.auth.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证管理")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<LoginVO> register(@Valid @RequestBody RegisterRequest request) {
        LoginVO vo = authService.register(request);
        return Result.success(HttpStatus.CREATED.value(), "注册成功", vo);
    }
    
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        LoginVO vo = authService.login(request);
        return Result.success(vo);
    }
    
    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.success();
    }
}
```

### 全局异常处理

```java
// GlobalExceptionHandler.java
package com.example.common.exception;

import com.example.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));
        log.warn("参数验证失败: {}", message);
        return Result.error(400, message);
    }
    
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBindException(BindException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));
        log.warn("参数绑定失败: {}", message);
        return Result.error(400, message);
    }
    
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBusinessException(BusinessException ex) {
        log.warn("业务异常: {}", ex.getMessage());
        return Result.error(ex.getCode(), ex.getMessage());
    }
    
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception ex) {
        log.error("系统异常", ex);
        return Result.error(500, "系统内部错误");
    }
}
```

## 6. 文件上传处理

### 配置文件上传

```yaml
# application.yml
spring:
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 100MB
      enabled: true

file:
  upload:
    path: ./uploads/
    allowed-types: jpg,jpeg,png,gif,bmp,webp
    max-size: 10485760  # 10MB
```

### 文件上传Controller

```java
// FileController.java
package com.example.modules.file.controller;

import com.example.common.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {
    
    @Value("${file.upload.path:./uploads}")
    private String uploadPath;
    
    @Value("${file.upload.allowed-types:jpg,jpeg,png,gif,bmp,webp}")
    private String allowedTypes;
    
    @Value("${file.upload.max-size:10485760}")
    private long maxSize;
    
    @PostMapping("/upload")
    public Result<String> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error(400, "上传文件不能为空");
        }
        
        // 验证文件大小
        if (file.getSize() > maxSize) {
            return Result.error(400, "文件大小不能超过10MB");
        }
        
        // 验证文件类型
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!allowedTypes.contains(extension)) {
            return Result.error(400, "不支持的文件类型");
        }
        
        try {
            // 生成唯一文件名
            String fileName = generateFileName(extension);
            
            // 创建目录
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            
            // 保存文件
            Path filePath = uploadDir.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // 返回文件访问URL
            String fileUrl = "/uploads/" + fileName;
            return Result.success(fileUrl);
            
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.error(500, "文件上传失败");
        }
    }
    
    @PostMapping("/upload/multiple")
    public Result<List<String>> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
        List<String> fileUrls = new ArrayList<>();
        
        for (MultipartFile file : files) {
            Result<String> result = uploadFile(file);
            if (result.getCode() == 200) {
                fileUrls.add(result.getData());
            }
        }
        
        return Result.success(fileUrls);
    }
    
    private String getFileExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(lastDotIndex + 1) : "";
    }
    
    private String generateFileName(String extension) {
        return UUID.randomUUID().toString().replace("-", "") + "." + extension;
    }
}
```

## 7. WebSocket实时通信

### WebSocket配置

```java
// WebSocketConfig.java
package com.example.common.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 启用简单Broker，用于订阅主题
        config.enableSimpleBroker("/topic", "/queue");
        // 应用程序目的地前缀
        config.setApplicationDestinationPrefixes("/app");
        // 用户目的地前缀
        config.setUserDestinationPrefix("/user/");
    }
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket端点
        registry.addEndpoint("/ws")
               .setAllowedOrigins("*")  // 生产环境应限制
               .withSockJS();
        
        // 不使用SockJS的端点
        registry.addEndpoint("/ws")
               .setAllowedOrigins("*");
    }
}
```

### 聊天消息处理

```java
// ChatController.java
package com.example.modules.chat.controller;

import com.example.common.result.Result;
import com.example.modules.chat.dto.ChatMessageDTO;
import com.example.modules.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    
    /**
     * WebSocket发送消息
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageDTO message) {
        // 保存消息到数据库（离线存储）
        chatService.saveMessage(message);
        
        // 发送给接收者
        messagingTemplate.convertAndSendToUser(
            message.getReceiverId().toString(),
            "/queue/messages",
            message
        );
        
        // 发送给发送者（确认）
        messagingTemplate.convertAndSendToUser(
            message.getSenderId().toString(),
            "/queue/messages",
            message
        );
    }
    
    /**
     * 获取对话列表
     */
    @GetMapping("/conversations")
    public Result<?> getConversations(@RequestAttribute("userId") Long userId) {
        return Result.success(chatService.getConversations(userId));
    }
    
    /**
     * 获取对话消息
     */
    @GetMapping("/conversations/{conversationId}/messages")
    public Result<?> getMessages(
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(chatService.getMessages(conversationId, page, size));
    }
}
```

### 消息实体

```java
// ChatMessageDTO.java
package com.example.modules.chat.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class ChatMessageDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long conversationId;
    private Long senderId;
    private Long receiverId;
    private String content;
    private Long timestamp;
}
```

## 8. MySQL集成最佳实践

### 数据库连接配置

```yaml
# application.yml
spring:
  datasource:
    url: "jdbc:mysql://localhost:3306/campus?serverTimezone=GMT%2B8&useUnicode=true&characterEncoding=utf8mb4"
    username: "root"
    password: "123"
    driver-class-name: com.mysql.cj.jdbc.Driver
    
    # 连接池配置（HikariCP）
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      idle-timeout: 30000
      pool-name: CampusHikariCP
      max-lifetime: 2000000
      connection-timeout: 30000
      connection-test-query: SELECT 1

# MyBatis-Plus配置
mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  type-aliases-package: com.example.modules.*.entity
  global-config:
    db-config:
      id-type: auto
      logic-delete-value: 0
      logic-not-delete-value: 1
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
```

### 性能优化建议

1. **索引优化**
   - 为高频查询字段创建索引
   - 使用复合索引优化多条件查询
   - 定期分析慢查询日志

2. **连接池配置**
   - 根据应用负载调整连接池大小
   - 设置合理的连接超时时间
   - 启用连接健康检查

3. **查询优化**
   - 使用分页查询减少数据加载量
   - 避免N+1查询问题
   - 使用MyBatis-Plus的批量操作

4. **数据持久化**
   - 使用事务管理确保数据一致性
   - 合理设置缓存策略
   - 定期备份数据库

## 9. Swagger API文档配置

### Swagger配置类

```java
// SwaggerConfig.java
package com.example.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("校园互助平台 API文档")
                .description("校园互助平台后端API接口文档")
                .version("v1.0.0")
                .contact(new Contact()
                    .name("Campus Team")
                    .email("team@campus.com"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0")))
            .schemaRequirement("Bearer", new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization"))
            .addSecurityItem(new SecurityRequirement().addList("Bearer"));
    }
}
```

### API文档注解示例

```java
// UserController.java
@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Operation(summary = "获取用户详情", description = "根据用户ID获取用户详细信息")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功"),
        @ApiResponse(responseCode = "404", description = "用户不存在")
    })
    @GetMapping("/{id}")
    public Result<UserVO> getUserById(@Parameter(description = "用户ID") @PathVariable Long id) {
        // ...
    }
}
```

## 总结

本研究报告涵盖了Spring Boot 3 + MyBatis-Plus构建REST API的核心最佳实践：

1. **项目结构**：采用按功能模块划分的清晰架构
2. **JWT认证**：基于Spring Security的标准实现模式
3. **CRUD操作**：充分利用MyBatis-Plus的强大功能
4. **数据库设计**：针对社交功能的优化表结构
5. **RESTful API**：规范的API设计和统一的响应格式
6. **文件上传**：基于Spring Boot multipart的可靠实现
7. **WebSocket**：实时通信的STOMP协议支持
8. **MySQL集成**：性能优化的连接配置
9. **API文档**：完整的Swagger/OpenAPI配置

这些实践基于Spring Boot和MyBatis-Plus的官方文档，提供了生产级别的最佳实践指导。

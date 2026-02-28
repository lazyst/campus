package com.campus.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * WebSocket CORS 过滤器
 * 处理 /ws/** 端点的跨域请求
 */
@Configuration
public class WebSocketCorsConfig {

    @Bean
    @Order(1)
    public FilterRegistrationBean<Filter> websocketCorsFilter() {
        FilterRegistrationBean<Filter> registrationBean = new FilterRegistrationBean<>(new Filter() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                    throws IOException, ServletException {
                HttpServletRequest httpRequest = (HttpServletRequest) request;
                HttpServletResponse httpResponse = (HttpServletResponse) response;

                String requestURI = httpRequest.getRequestURI();

                // 只对 /ws/** 路径应用 CORS
                if (requestURI.contains("/ws/")) {
                    String origin = httpRequest.getHeader("Origin");
                    if (origin != null && (origin.equals("http://localhost:3000") || 
                        origin.equals("http://localhost:3001") ||
                        origin.equals("http://127.0.0.1:3000") || 
                        origin.equals("http://127.0.0.1:3001"))) {
                        httpResponse.setHeader("Access-Control-Allow-Origin", origin);
                        httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
                    }
                    httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                    httpResponse.setHeader("Access-Control-Allow-Headers", "*");

                    // 处理 OPTIONS 预检请求
                    if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
                        httpResponse.setStatus(HttpServletResponse.SC_OK);
                        return;
                    }
                }

                chain.doFilter(request, response);
            }

            @Override
            public void init(FilterConfig filterConfig) throws ServletException {
            }

            @Override
            public void destroy() {
            }
        });

        registrationBean.setName("websocketCorsFilter");
        registrationBean.addUrlPatterns("/ws/*");
        registrationBean.setEnabled(true);
        registrationBean.setOrder(1); // 最高优先级

        return registrationBean;
    }
}

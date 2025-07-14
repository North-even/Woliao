package com.woliao.backend.security;

import com.woliao.backend.service.CustomUserDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                String username = jwtTokenProvider.getUsernameFromToken(jwt);

                // 当令牌有效，并且当前安全上下文中没有认证信息时，进行设置
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                    // 验证令牌对于该用户是否有效
                    if (jwtTokenProvider.validateToken(jwt, userDetails)) {
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        // 在上下文中设置认证信息
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            }
        } catch (ExpiredJwtException ex) {
            // 当令牌过期时，只记录警告，不抛出异常，让后续流程处理未认证状态
            logger.warn("JWT token has expired: " + ex.getMessage());
        } catch (Exception ex) {
            // 其他异常则记录错误
            logger.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        // 1. 优先从 "Authorization" 请求头获取令牌
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        // 2. 如果请求头中没有，再尝试从URL参数 "token" 获取 (为WebSocket专门设计)
        String tokenParam = request.getParameter("token");
        if (StringUtils.hasText(tokenParam)) {
            return tokenParam;
        }

        return null;
    }
}
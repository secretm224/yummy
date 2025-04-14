package com.cho_co_song_i.yummy.yummy.jwt;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String referer = request.getHeader("referer");
        String requestUrl = request.getRequestURL().toString();
        boolean isSwaggerCall = (referer != null && referer.contains("swagger") && requestUrl != null && requestUrl.contains("login"));
        boolean isGraphQLCall = requestUrl != null && requestUrl.contains("/graphql");

        if (!isSwaggerCall && !isGraphQLCall) {
            // Swagger UI가 아닌 요청은 필터 무시
            filterChain.doFilter(request, response);
            return;
        }

        // Swagger UI 요청일 경우 JWT 검증 진행
        String token = jwtTokenProvider.resolveToken(request);
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: Invalid or missing token.");
            return;
        }

        Authentication auth = jwtTokenProvider.getAuthentication(token);
        SecurityContextHolder.getContext().setAuthentication(auth);
        filterChain.doFilter(request, response);
    }
}

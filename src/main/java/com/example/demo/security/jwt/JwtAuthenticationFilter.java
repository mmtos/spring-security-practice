package com.example.demo.security.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Getter
@Builder
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public static JwtAuthenticationFilter of(JwtTokenProvider jwtTokenProvider) {
        return JwtAuthenticationFilter.builder()
                .jwtTokenProvider(jwtTokenProvider)
                .build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if(authorizationHeader == null){
            filterChain.doFilter(request, response);
            return;
        }
        try {
            // 실질적인 인증 로직
            UserDetails user = jwtTokenProvider.getUserDetailsOf(authorizationHeader);

            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(user.getUsername(),"",user.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(token);
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException exception) {
            logger.error("ExpiredJwtException", exception);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().print("Token Expired.");
        }
    }
}

package com.example.demo.security.jwt;

import com.example.demo.domain.dto.MyUserDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

@RequiredArgsConstructor
@Component
public class JwtTokenProvider {
    private final JwtProperties jwtProperties;
    private final UserDetailsService myUserDetailService;

    public String makeJwtToken(MyUserDto user) {
        ZonedDateTime now = LocalDateTime.now().atZone(ZoneId.systemDefault());
        ZonedDateTime expirationTime = now.plusHours(1);
        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setIssuer(jwtProperties.getIssuer())
                .setIssuedAt(Date.from(now.toInstant()))
                .setExpiration(Date.from(expirationTime.toInstant()))
                .claim("username", user.getUsername()) // Public claim
                .signWith(SignatureAlgorithm.HS256, jwtProperties.getSecretKey())
                .compact();
    }

    public UserDetails getUserDetailsOf(String authorizationHeader) {
        validationAuthorizationHeader(authorizationHeader);

        String token = extractToken(authorizationHeader);
        Claims claims = parsingToken(token);

        return myUserDetailService.loadUserByUsername(claims.get("username",String.class));
    }

    private Claims parsingToken(String token) {
        return Jwts.parser()
                .setSigningKey(jwtProperties.getSecretKey())
                .parseClaimsJws(token)
                .getBody();
    }

    private void validationAuthorizationHeader(String header) {
        if (header == null || !header.startsWith(jwtProperties.getTokenPrefix())) {
            throw new IllegalArgumentException();
        }
    }

    private String extractToken(String authorizationHeader) {
        return authorizationHeader.substring(jwtProperties.getTokenPrefix().length());
    }
}

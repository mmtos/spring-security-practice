package com.example.demo.security.handler;

import com.example.demo.domain.dto.MyUserDto;
import com.example.demo.security.jwt.JwtTokenProvider;
import com.example.demo.security.userdetails.MyUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        MyUserDto myUserDto = new MyUserDto();
        myUserDto.setUsername(((MyUserDetails) authentication.getPrincipal()).getUsername());
        response.getWriter().print(jwtTokenProvider.makeJwtToken(myUserDto));
        response.flushBuffer();
    }
}

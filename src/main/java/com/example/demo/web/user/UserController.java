package com.example.demo.web.user;

import com.example.demo.domain.entity.MyUser;
import com.example.demo.security.userdetails.MyUserDetailService;
import com.example.demo.web.user.dto.JoinUserRequestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class UserController {

    @Autowired
    private MyUserDetailService userService;

    @GetMapping("/join")
    public String joinPage(){
        return "join";
    }

    @PostMapping("/join")
    public String joinProcess(JoinUserRequestDto joinUserDto){
        userService.createUser(
                MyUser.builder()
                        .username(joinUserDto.getUsername())
                        .password(joinUserDto.getPassword())
                        .isEnabled(true).build());
        return "redirect:home";
    }

}

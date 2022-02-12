package com.example.demo.web;

import com.example.demo.entity.MyUser;
import com.example.demo.userservice.MyUserDetailService;
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
    public String joinProcess(MyUser user){
        userService.createUser(MyUser.builder().username(user.getUsername()).password(user.getPassword()).isEnabled(true).build());
        return "redirect:home";
    }
}

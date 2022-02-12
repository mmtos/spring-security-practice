package com.example.demo.userservice;

import com.example.demo.entity.MyUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<MyUser,String>{
    MyUser findByUsername(String username);
}

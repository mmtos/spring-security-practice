package com.example.demo.domain.repository;

import com.example.demo.domain.entity.MyUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<MyUser,String>{
    MyUser findByUsername(String username);
}

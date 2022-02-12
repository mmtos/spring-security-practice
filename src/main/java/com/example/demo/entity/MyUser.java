package com.example.demo.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Getter
@Setter
public class MyUser {
    @Id
    private String userid;

    @Column(nullable = false, unique = true)
    private String username;

    private String password;

    private boolean isEnabled;
}

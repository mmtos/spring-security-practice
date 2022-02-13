package com.example.demo.domain.entity;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MyUser {
    @Id
    private String username;

    private String password;

    private Boolean isEnabled;
}

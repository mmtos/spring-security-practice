package com.example.demo.userservice;

import com.example.demo.entity.MyUser;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
@AllArgsConstructor
public class MyUserDetails implements UserDetails {

    private MyUser myUser;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // UserDetails 반드시 확인 - null return 불가능
        return AuthorityUtils.NO_AUTHORITIES;
    }

    @Override
    public String getPassword() {
        return myUser.getPassword();
    }

    @Override
    public String getUsername() {
        return myUser.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return myUser.getIsEnabled();
    }
}

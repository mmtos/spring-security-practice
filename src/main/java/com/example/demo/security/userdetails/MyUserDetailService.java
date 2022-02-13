package com.example.demo.security.userdetails;

import com.example.demo.domain.entity.MyUser;
import com.example.demo.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
/**
 * 스프링 시큐리티 설정시 UserDetailsService 빈을 자동으로 scan해서 등록됨
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MyUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        MyUser user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException(username);
        }
        return new MyUserDetails(user);
    }

    public void createUser(MyUser user) {
        userRepository.save(user);
    }

    public void updateUser(MyUser user) {
        userRepository.save(user);
    }

    public void deleteUser(String username) {
        userRepository.deleteById(username);
    }

    public boolean userExists(String username) {
        return userRepository.findByUsername(username) != null;
    }
}

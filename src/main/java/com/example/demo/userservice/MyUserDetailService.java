package com.example.demo.userservice;

import com.example.demo.entity.MyUser;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
/**
 * 스프링 시큐리티 설정시 UserDetailsService 빈을 자동으로 scan해서 등록됨
 */
@Service
@Setter
@Slf4j
public class MyUserDetailService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    private AuthenticationManager authenticationManager;

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

    public void changePassword(String oldPassword, String newPassword) throws AuthenticationException {
        Authentication currentUser = SecurityContextHolder.getContext().getAuthentication();
        if (currentUser == null) {
            // This would indicate bad coding somewhere
            throw new AccessDeniedException(
                    "Can't change password as no Authentication object found in context " + "for current user.");
        }
        String username = currentUser.getName();
        // If an authentication manager has been set, re-authenticate the user with the
        // supplied password.
        if (this.authenticationManager != null) {
            log.debug("Reauthenticating user {} for password change request.", username);
            this.authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, oldPassword));
        }
        else {
            log.debug("No authentication manager set. Password won't be re-checked.");
        }
        log.debug("Changing password for user '" + username + "'");
        MyUser changePasswordUser= MyUser.builder().username(currentUser.getName()).isEnabled(true).password(newPassword).build();
        userRepository.save(changePasswordUser);

        Authentication authentication = createNewAuthentication(currentUser, newPassword);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    protected Authentication createNewAuthentication(Authentication currentAuth, String newPassword) {
        UserDetails user = loadUserByUsername(currentAuth.getName());
        UsernamePasswordAuthenticationToken newAuthentication = new UsernamePasswordAuthenticationToken(user, null,
                user.getAuthorities());
        newAuthentication.setDetails(currentAuth.getDetails());
        return newAuthentication;
    }

    public boolean userExists(String username) {
        return userRepository.findByUsername(username) != null;
    }
}

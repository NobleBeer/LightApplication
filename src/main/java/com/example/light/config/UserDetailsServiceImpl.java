package com.example.light.config;

import com.example.light.entity.User;
import com.example.light.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Override
    public CustomUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        logger.debug("Entering in loadUserByUsername Method...");
        User user = userRepository.findByName(username);
        if (user == null) {
            logger.debug("Username not found: " + username);
            throw new UsernameNotFoundException("could not found user..!!");
        }
        logger.debug("User name {} and id {}", user.getName(), user.getId());
        return CustomUserDetails.build(user);
    }

    public void createUser(User user) {
        userRepository.save(user);
    }

    public User getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }
        logger.debug("auth {}", authentication.getName());
        Object principal = authentication.getPrincipal();
        logger.debug("principal {}", principal.toString());
        if (!(principal instanceof CustomUserDetails userDetails)) {
            throw new IllegalStateException("Principal is not an instance of CustomUserDetails");
        }

        logger.debug(" userDetails {}", userDetails);
        return userRepository.getReferenceById(userDetails.getId());
    }

}
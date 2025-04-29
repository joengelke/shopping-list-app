package com.joengelke.shoppinglistapp.backend.service;

import com.joengelke.shoppinglistapp.backend.model.User;
import com.joengelke.shoppinglistapp.backend.repository.UserRepository;
import com.joengelke.shoppinglistapp.backend.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;


@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public UserService(UserRepository userRepository, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), getAuthority(user));
    }

    private Set<SimpleGrantedAuthority> getAuthority(User user) {
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        user.getRoles().forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
        return authorities;
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }


    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getAllUserByIds(List<String> userIds) {
        return userRepository.findAllById(userIds);
    }

    public User addRoleToUser(String userId, String role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        if(!user.getRoles().contains(role.toUpperCase())) {
            user.getRoles().add(role.toUpperCase());
        }
        return userRepository.save(user);
    }

    public User removeRoleFromUser(String userId, String role, String header) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        // Check: role must exist before removing
        if(!user.getRoles().contains(role.toUpperCase())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User does not have the role: " + role.toUpperCase()); // Code 409
        }

        // Check: prevent removing last role
        if (user.getRoles().size() == 1) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "User must have at least one role."); // Code 422
        }

        // Check: prevent removing ADMIN from yourself
        String myUserId = jwtTokenProvider.getUserIdFromToken(header.replace("Bearer ", ""));
        if(myUserId.equals(userId) && role.equalsIgnoreCase("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot remove your own ADMIN role.");
        }

        user.getRoles().remove(role.toUpperCase());
        return userRepository.save(user);
    }
}

package com.joengelke.shoppinglistapp.backend.service;

import com.joengelke.shoppinglistapp.backend.dto.UserResponse;
import com.joengelke.shoppinglistapp.backend.model.User;
import com.joengelke.shoppinglistapp.backend.repository.RecipeRepository;
import com.joengelke.shoppinglistapp.backend.repository.UserRepository;
import com.joengelke.shoppinglistapp.backend.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, JwtTokenProvider jwtTokenProvider, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
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

    public User createUser(String username, String password) {
        return userRepository.save(new User(username, password));
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();

        return users.stream()
                .map(UserResponse::new) // Mapping each User to UserResponse
                .collect(Collectors.toList());
    }

    public List<UserResponse> getAllUserByIds(List<String> userIds) {
        List<User> users = userRepository.findAllById(userIds);

        // Map each User to UserResponse
        return users.stream()
                .map(UserResponse::new) // Mapping each User to UserResponse
                .collect(Collectors.toList());
    }

    public UserResponse addRoleToUser(String userId, String role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        if (!user.getRoles().contains(role.toUpperCase())) {
            user.getRoles().add(role.toUpperCase());
        }
        userRepository.save(user);
        return new UserResponse(user);
    }

    public UserResponse removeRoleFromUser(String userId, String role, String header) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        // Check: role must exist before removing
        if (!user.getRoles().contains(role.toUpperCase())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User does not have the role: " + role.toUpperCase()); // Code 409
        }

        // Check: prevent removing last role
        if (user.getRoles().size() == 1) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "User must have at least one role."); // Code 422
        }

        // Check: prevent removing ADMIN from yourself
        String myUserId = jwtTokenProvider.getUserIdFromToken(header.replace("Bearer ", ""));
        if (myUserId.equals(userId) && role.equalsIgnoreCase("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot remove your own ADMIN role.");
        }

        user.getRoles().remove(role.toUpperCase());
        userRepository.save(user);
        return new UserResponse(user);
    }

    public UserResponse changeUsername(String newUsername, String header) {
        if (userRepository.existsByUsername(newUsername)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already taken");
        }
        String userId = jwtTokenProvider.getUserIdFromToken(header.replace("Bearer ", ""));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        user.setUsername(newUsername);
        userRepository.save(user);
        return new UserResponse(user);
    }

    public UserResponse changePassword(String currentPassword, String newPassword, String header) {
        String userId = jwtTokenProvider.getUserIdFromToken(header.replace("Bearer ", ""));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Current password is incorrect");
        }
        if (currentPassword.equals(newPassword)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password cannot be the same as the current password");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return new UserResponse(user);
    }

    public UserResponse addRecipeToUser(String userId, String recipeId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        // Make sure the list is initialized
        if (user.getRecipeIds() == null) {
            user.setRecipeIds(new ArrayList<>());
        }

        if (!user.getRecipeIds().contains(recipeId)) {
            user.getRecipeIds().add(recipeId);
        }
        userRepository.save(user);
        return new UserResponse(user);
    }

    public void removeRecipeFromUser(String userId, String recipeId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        List<String> recipeIds = user.getRecipeIds();
        if (recipeIds != null && recipeIds.contains(recipeId)) {
            recipeIds.remove(recipeId);
            user.setRecipeIds(recipeIds);
            userRepository.save(user);
        }
    }

    public void removeRecipeFromAllUsers(String recipeId) {
        List<User> usersWithRecipe = userRepository.findByRecipeIdsContaining(recipeId);
        for (User user : usersWithRecipe) {
            user.getRecipeIds().remove(recipeId);
        }
        userRepository.saveAll(usersWithRecipe);
    }

    public List<String> getRecipeIdsByUserId(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        return user.getRecipeIds();
    }

    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }


}

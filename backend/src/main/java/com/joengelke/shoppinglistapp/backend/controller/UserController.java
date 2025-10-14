package com.joengelke.shoppinglistapp.backend.controller;

import com.joengelke.shoppinglistapp.backend.dto.ChangePasswordRequest;
import com.joengelke.shoppinglistapp.backend.dto.ChangeUsernameRequest;
import com.joengelke.shoppinglistapp.backend.dto.UserResponse;
import com.joengelke.shoppinglistapp.backend.model.User;
import com.joengelke.shoppinglistapp.backend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers() {
        List<UserResponse> userList = userService.getAllUsers();
        return ResponseEntity.ok(userList);
    }

    @GetMapping("/recipe-ids")
    public ResponseEntity<?> getCurrentUserRecipeIds(@RequestHeader("Authorization") String header) {
        List<String> recipeIds = userService.getCurrentUserRecipeIds(header);
        return ResponseEntity.ok(recipeIds);
    }

    @PutMapping("/{userId}/add-role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addRoleToUser(@PathVariable String userId, @RequestParam String role) {
        UserResponse user = userService.addRoleToUser(userId, role);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{userId}/remove-role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> removeRoleFromUser(
            @PathVariable String userId,
            @RequestParam String role,
            @RequestHeader("Authorization") String header) {
        try {
            UserResponse user = userService.removeRoleFromUser(userId, role, header);
            return ResponseEntity.ok(user);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }

    @PutMapping("/username")
    public ResponseEntity<?> changeUsername(@RequestBody ChangeUsernameRequest request, @RequestHeader("Authorization") String header) {
        try {
            UserResponse user = userService.changeUsername(request.getNewUsername(), header);
            return ResponseEntity.ok(user);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request, @RequestHeader("Authorization") String header) {
        try {
            UserResponse user = userService.changePassword(request.getCurrentPassword(), request.getNewPassword(), header);
            return ResponseEntity.ok(user);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }

}

package com.joengelke.shoppinglistapp.backend.dto;

import com.joengelke.shoppinglistapp.backend.model.User;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserResponse {
    private String id;
    private String username;
    private String email;
    private List<String> roles;
    private List<String> recipeIds;

    public UserResponse(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.roles = user.getRoles();
        this.recipeIds = user.getRecipeIds();
    }
}

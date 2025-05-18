package com.joengelke.shoppinglistapp.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeUsernameRequest {
    private String newUsername;
}
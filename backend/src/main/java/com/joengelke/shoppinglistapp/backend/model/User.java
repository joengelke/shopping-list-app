package com.joengelke.shoppinglistapp.backend.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Document
public class User {

    @Id
    private String id;
    private String username;
    private String password;
    private String email;
    private List<String> roles; // ["USER","ADMIN"]

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.roles = new ArrayList<String>(List.of("USER"));

    }
    public User(){}
}

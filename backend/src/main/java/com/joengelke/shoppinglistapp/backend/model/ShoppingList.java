package com.joengelke.shoppinglistapp.backend.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Document
public class ShoppingList {

    @Id
    private String id;
    private String name;
    private Date createdAt;
    private List<String> itemIds;

    public ShoppingList(String id, String name) {
        this.id = id;
        this.name = name;
        this.createdAt = new Date();
        this.itemIds = new ArrayList<>();
    }
}

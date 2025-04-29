package com.joengelke.shoppinglistapp.backend.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Document
public class ShoppingList {

    @Id
    private String id;
    private String name;
    private Instant createdAt;
    private List<String> itemIds;
    private List<String> itemSetIds;
    private List<String> userIds;

    public ShoppingList(String id, String name) {
        this.id = id;
        this.name = name;
        this.createdAt = Instant.now();
        this.itemIds = new ArrayList<>();
        this.itemSetIds = new ArrayList<>();
        this.userIds = new ArrayList<>();
    }

    public ShoppingList(){}

}

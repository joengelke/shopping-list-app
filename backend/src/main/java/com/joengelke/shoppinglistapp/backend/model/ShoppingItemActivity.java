package com.joengelke.shoppinglistapp.backend.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@Document
public class ShoppingItemActivity {

    @Id
    private String id;
    private String listId;
    private String userId;
    private String itemId;
    private String name;
    private double amount;
    private String unit;
    private Instant timestamp;
    private String actionType; // ITEM_CHECKED, ITEM_DELETED, ITEM_EDITED

    public ShoppingItemActivity(String listId, String userId, String itemId, String name, double amount, String unit, Instant timestamp, String actionType) {
        this.listId = listId;
        this.userId = userId;
        this.itemId = itemId;
        this.name = name;
        this.amount = amount;
        this.unit = unit;
        this.timestamp = timestamp;
        this.actionType = actionType;
    }

    public ShoppingItemActivity(){}
}

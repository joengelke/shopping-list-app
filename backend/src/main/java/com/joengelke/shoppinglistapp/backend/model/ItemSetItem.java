package com.joengelke.shoppinglistapp.backend.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemSetItem {
    private String id; // matches ShoppingList id if it exists
    private String tmpId; // id to use in frontend
    private String name;
    private Double amount;
    private String unit;

    public ItemSetItem(String id, String tmpId, String name, Double amount, String unit) {
        this.id = id;
        this.tmpId = tmpId;
        this.name = name;
        this.amount = amount;
        this.unit = unit;
    }

    public ItemSetItem(){}
}

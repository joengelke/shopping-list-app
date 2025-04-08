package com.joengelke.shoppinglistapp.backend.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@Document
public class ShoppingItem {

    @Id
    private String id;
    private String name;
    private String category;
    private Double amount;
    private String unit;
    private boolean checked;
    private String note;
    private Instant editedAt;
    private String editedBy;

    public ShoppingItem(String name, String category, Double amount, String unit, String note, String editedBy) {
        this.name = name;
        this.category = category;
        this.amount = amount;
        this.unit = unit;
        this.checked = false;
        this.note = note;
        this.editedAt = Instant.now();
        this.editedBy = editedBy;
    }

    public ShoppingItem(){}
}

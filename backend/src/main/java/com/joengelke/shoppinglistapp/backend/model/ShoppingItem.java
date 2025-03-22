package com.joengelke.shoppinglistapp.backend.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

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
    @JsonFormat(shape = JsonFormat.Shape.STRING,
            pattern = "dd.MM.yyyy HH:mm:ss") // German format: 22.03.2025 12:37:55
    private Date editedAt;
    private String editedBy;

    public ShoppingItem(String id, String name, String category, Double amount, String unit, String note, String editedBy) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.amount = amount;
        this.unit = unit;
        this.checked = false;
        this.note = note;
        this.editedAt = new Date();
        this.editedBy = editedBy;
    }
}

package com.joengelke.shoppinglistapp.backend.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShoppingItem {

    @Id
    private String id;
    private String name;
    private List<String> tags;
    private Double amount;
    private String unit;
    private boolean checked;
    private Instant checkedAt;
    private String note;
    private Instant editedAt;
    private String editedBy;

    public ShoppingItem(String name, List<String> tags, Double amount, String unit, String note, String editedBy) {
        this.name = name;
        this.tags = tags;
        this.amount = amount;
        this.unit = unit;
        this.checked = false;
        this.checkedAt = Instant.now();
        this.note = note;
        this.editedAt = Instant.now();
        this.editedBy = editedBy;
    }

    public ShoppingItem(){}
}

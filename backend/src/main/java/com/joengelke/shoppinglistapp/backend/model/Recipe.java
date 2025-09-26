package com.joengelke.shoppinglistapp.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@Document
public class Recipe {

    @Id
    private String id;
    private String name; //default is itemSetName
    private String creatorId;
    private Instant createdAt;
    private ItemSet itemSet;
    private String description;
    private List<String> instructions;
    private List<String> categories;
    private Visibility visibility;
    private List<String> sharedWithUserIds;
    private List<String> recipeFileIds;

    public Recipe() {}

    public Recipe(String name, String creatorId, ItemSet itemSet, String description, List<String> instructions, List<String> categories, Visibility visibility, List<String> sharedWithUserIds, List<String> recipeFileIds) {
        this.name = name != null ? name : itemSet.getName();
        this.creatorId = creatorId;
        this.createdAt = Instant.now();
        this.itemSet = itemSet;
        this.description = description != null ? description : "";
        this.instructions = instructions != null ? instructions : new ArrayList<>();
        this.categories = categories != null ? categories : new ArrayList<>();
        this.visibility = visibility != null ? visibility : Visibility.PRIVATE;
        this.sharedWithUserIds = sharedWithUserIds != null ? sharedWithUserIds : new ArrayList<>();
        this.recipeFileIds = recipeFileIds != null ? recipeFileIds : new ArrayList<>();
    }
}
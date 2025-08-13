package com.joengelke.shoppinglistapp.backend.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@Document
public class ItemSet {

    @Id
    private String id;
    private String name;
    private List<ItemSetItem> itemList;
    private String receiptFileId = "";

    public ItemSet(String name, List<ItemSetItem> itemList, String receiptFileId) {
        this.name = name;
        this.itemList = itemList;
        this.receiptFileId = receiptFileId;
    }

    public ItemSet(){}
}

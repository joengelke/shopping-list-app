package com.joengelke.shoppinglistapp.backend.service;

import com.joengelke.shoppinglistapp.backend.model.ShoppingItem;
import com.joengelke.shoppinglistapp.backend.repository.ShoppingItemRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.NoSuchElementException;

@Service
public class ShoppingItemService {

    private final ShoppingItemRepository shoppingItemRepository;

    public ShoppingItemService(ShoppingItemRepository shoppingItemRepository) {
        this.shoppingItemRepository = shoppingItemRepository;
    }

    public ShoppingItem createItem(ShoppingItem shoppingItem) {
        if (shoppingItem.getName() == null) {
            shoppingItem.setName("");
        }
        if (shoppingItem.getCategory() == null) {
            shoppingItem.setCategory("");
        }
        if (shoppingItem.getAmount() == null) {
            shoppingItem.setAmount(1.0);
        }
        if (shoppingItem.getUnit() == null) {
            shoppingItem.setUnit("");
        }
        if (shoppingItem.getNote() == null) {
            shoppingItem.setNote("");
        }
        if (shoppingItem.getEditedAt() == null) {
            shoppingItem.setEditedAt(new Date());
        }
        if (shoppingItem.getCreatedBy() == null) {
            shoppingItem.setCreatedBy("");
        }
        return shoppingItemRepository.save(shoppingItem);
    }

    public ShoppingItem getItemById(String id) {
        return shoppingItemRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Item not found with itemId: " + id));
    }

    public ShoppingItem updateItem(ShoppingItem newShoppingItem) {
        ShoppingItem shoppingItem = shoppingItemRepository.findById(newShoppingItem.getId())
                .orElseThrow(() -> new NoSuchElementException("Item not found with itemId: " + newShoppingItem.getId()));

        // checked status and createdBy cant be updated
        if (newShoppingItem.getName() != null) {
            shoppingItem.setName(newShoppingItem.getName());
        }
        if (newShoppingItem.getCategory() != null) {
            shoppingItem.setCategory(newShoppingItem.getCategory());
        }
        if (newShoppingItem.getAmount() != null) {
            shoppingItem.setAmount(newShoppingItem.getAmount());
        }
        if (newShoppingItem.getUnit() != null) {
            shoppingItem.setUnit(newShoppingItem.getUnit());
        }
        if (newShoppingItem.getNote() != null) {
            shoppingItem.setNote(newShoppingItem.getNote());
        }
        if (newShoppingItem.getEditedAt() != null) {
            shoppingItem.setEditedAt(new Date());
        }
        return shoppingItemRepository.save(shoppingItem);
    }

    // just change checked status for better performance
    public ShoppingItem updateCheckedStatus(String itemId, boolean checked) {
        ShoppingItem shoppingItem = shoppingItemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Item not found with itemId: " + itemId));
        shoppingItem.setChecked(checked);
        return shoppingItemRepository.save(shoppingItem);
    }

    public void deleteItemById(String id) {
        shoppingItemRepository.deleteById(id);
    }

}

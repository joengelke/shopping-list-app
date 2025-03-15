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
        return shoppingItemRepository.save(shoppingItem);
    }

    public ShoppingItem getItemById(String id) {
        return shoppingItemRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Item not found with itemId: " + id));
    }

    public ShoppingItem updateItem(ShoppingItem newShoppingItem) {
        ShoppingItem shoppingItem = shoppingItemRepository.findById(newShoppingItem.getId())
                .orElseThrow(() -> new NoSuchElementException("Item not found with itemId: " + newShoppingItem.getId()));

        shoppingItem.setName(newShoppingItem.getName());
        shoppingItem.setCategory(newShoppingItem.getCategory());
        shoppingItem.setAmount(newShoppingItem.getAmount());
        shoppingItem.setUnit(newShoppingItem.getUnit());
        shoppingItem.setChecked(newShoppingItem.isChecked());
        shoppingItem.setNote(newShoppingItem.getNote());
        shoppingItem.setEditedAt(new Date());

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

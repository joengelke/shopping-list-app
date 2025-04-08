package com.joengelke.shoppinglistapp.backend.service;

import com.joengelke.shoppinglistapp.backend.model.ShoppingItem;
import com.joengelke.shoppinglistapp.backend.repository.ShoppingItemRepository;
import com.joengelke.shoppinglistapp.backend.security.JwtTokenProvider;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Objects;

@Service
public class ShoppingItemService {

    private final ShoppingItemRepository shoppingItemRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public ShoppingItemService(ShoppingItemRepository shoppingItemRepository, JwtTokenProvider jwtTokenProvider) {
        this.shoppingItemRepository = shoppingItemRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    // creates one new item
    public ShoppingItem createItem(String header, ShoppingItem shoppingItem) {

        String username = jwtTokenProvider.getUsernameFromToken(header.replace("Bearer ", ""));

        if (Objects.equals(shoppingItem.getId(), "")) {
            shoppingItem.setId(null);
        }
        if (shoppingItem.getName() == null) {
            shoppingItem.setName("");
        }
        if (shoppingItem.getCategory() == null) {
            shoppingItem.setCategory("");
        }

        if (shoppingItem.getUnit() == null) {
            shoppingItem.setUnit("");
        }
        if (shoppingItem.getNote() == null) {
            shoppingItem.setNote("");
        }
        if (shoppingItem.getEditedAt() == null) {
            shoppingItem.setEditedAt(Instant.now());
        }
        shoppingItem.setAmount(0.0);

        shoppingItem.setChecked(false);

        shoppingItem.setEditedAt(Instant.now());

        shoppingItem.setEditedBy(username);

        return shoppingItemRepository.save(shoppingItem);
    }

    public ShoppingItem getItemById(String id) {
        return shoppingItemRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Item not found with itemId: " + id));
    }

    public ShoppingItem updateItem(String header, ShoppingItem newShoppingItem) {
        ShoppingItem shoppingItem = shoppingItemRepository.findById(newShoppingItem.getId())
                .orElseThrow(() -> new NoSuchElementException("Item not found with itemId: " + newShoppingItem.getId()));

        String username = jwtTokenProvider.getUsernameFromToken(header.replace("Bearer ", ""));

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

        shoppingItem.setChecked(newShoppingItem.isChecked());

        shoppingItem.setEditedAt(Instant.now());

        shoppingItem.setEditedBy(username);

        return shoppingItemRepository.save(shoppingItem);
    }

    // just change checked status for better performance
    public ShoppingItem updateCheckedStatus(String itemId, boolean checked) {
        ShoppingItem shoppingItem = shoppingItemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Item not found with itemId: " + itemId));
        shoppingItem.setChecked(checked);
        return shoppingItemRepository.save(shoppingItem);
    }

    // remove one item amount
    public ShoppingItem removeOneItemById(String itemId) {
        // return null if item was deleted or the item if amount was updated
        ShoppingItem shoppingItem = shoppingItemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Item not found with itemId: " + itemId));
        if (shoppingItem.getAmount() >= 1) {
            // item exists, reduce amount
            shoppingItem.setAmount(shoppingItem.getAmount() - 1);
        }  else {
            // checks item so its not on active list anymore
            shoppingItem.setChecked(true);
        }
        return shoppingItemRepository.save(shoppingItem);
    }

    public void deleteItemById(String id) {
        shoppingItemRepository.deleteById(id);
    }


}

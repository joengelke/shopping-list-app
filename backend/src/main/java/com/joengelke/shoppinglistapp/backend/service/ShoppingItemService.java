package com.joengelke.shoppinglistapp.backend.service;

import com.joengelke.shoppinglistapp.backend.model.ShoppingItem;
import com.joengelke.shoppinglistapp.backend.repository.ShoppingItemRepository;
import com.joengelke.shoppinglistapp.backend.security.JwtTokenProvider;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.NoSuchElementException;

@Service
public class ShoppingItemService {

    private final ShoppingItemRepository shoppingItemRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public ShoppingItemService(ShoppingItemRepository shoppingItemRepository, JwtTokenProvider jwtTokenProvider) {
        this.shoppingItemRepository = shoppingItemRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public ShoppingItem addOneItem(String header, ShoppingItem shoppingItem) {

        String username = jwtTokenProvider.getUsernameFromToken(header.replace("Bearer ", ""));

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

        shoppingItem.setEditedAt(new Date());

        shoppingItem.setEditedBy(username);


        ShoppingItem existingItem = shoppingItemRepository.findByName(shoppingItem.getName());
        if (existingItem != null) {
            existingItem.setAmount(existingItem.getAmount() + 1);
            existingItem.setEditedAt(new Date());
            return shoppingItemRepository.save(existingItem);
        }

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

        shoppingItem.setEditedAt(new Date());

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

    public ShoppingItem removeOneItem(String itemId) {
        // return null if item was deleted or the item if amount was updated
        ShoppingItem shoppingItem = shoppingItemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Item not found with itemId: " + itemId));
        if (shoppingItem.getAmount() > 1) {
            shoppingItem.setAmount(shoppingItem.getAmount() - 1);
        } else {
            shoppingItemRepository.deleteById(shoppingItem.getId());
            return null;
        }
        return shoppingItemRepository.save(shoppingItem);
    }

    public void deleteItemById(String id) {
        shoppingItemRepository.deleteById(id);
    }


}

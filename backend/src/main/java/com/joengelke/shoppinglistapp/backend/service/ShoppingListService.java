package com.joengelke.shoppinglistapp.backend.service;

import com.joengelke.shoppinglistapp.backend.model.ShoppingItem;
import com.joengelke.shoppinglistapp.backend.model.ShoppingList;
import com.joengelke.shoppinglistapp.backend.repository.ShoppingListRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class ShoppingListService {

    // TODO add WebSockets ?

    private final ShoppingListRepository shoppingListRepository;
    private final ShoppingItemService shoppingItemService;

    public ShoppingListService(ShoppingListRepository shoppingListRepository, ShoppingItemService shoppingItemService) {
        this.shoppingListRepository = shoppingListRepository;
        this.shoppingItemService = shoppingItemService;
    }

    public ShoppingList createShoppingList(ShoppingList shoppingList) {
        if (shoppingList.getName() == null) {
            shoppingList.setName("");
        }
        if (shoppingList.getCreatedAt() == null) {
            shoppingList.setCreatedAt(Instant.now());
        }
        if (shoppingList.getItemIds() == null) {
            shoppingList.setItemIds(new ArrayList<>());
        }
        return shoppingListRepository.save(shoppingList);
    }

    public List<ShoppingList> getAllShoppingLists() {
        return shoppingListRepository.findAll();
    }

    public ShoppingList getShoppingListById(String id) {

        return shoppingListRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Shopping list not found"));
    }

    public ShoppingList updateShoppingList(ShoppingList newShoppingList) {
        ShoppingList shoppingList = shoppingListRepository.findById(newShoppingList.getId())
                .orElseThrow(() -> new NoSuchElementException("Shopping list not found"));

        // update ShoppingList attributes
        if (newShoppingList.getName() != null) {
            shoppingList.setName(newShoppingList.getName());
        }
        return shoppingListRepository.save(shoppingList);
    }

    public void deleteShoppingList(String id) {
        ShoppingList shoppingList = shoppingListRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Shopping list not found"));

        // delete each item in list
        for (String itemId : shoppingList.getItemIds()) {
            shoppingItemService.deleteItemById(itemId);
        }

        shoppingListRepository.deleteById(id);
    }

    // returns shoppingItemList
    public List<ShoppingItem> getItemsByShoppingList(String id) {
        ShoppingList shoppingList = shoppingListRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Shopping list not found"));

        List<ShoppingItem> itemList = new ArrayList<>();
        for (String itemId : shoppingList.getItemIds()) {
            itemList.add(shoppingItemService.getItemById(itemId));
        }
        return itemList;
    }

    // return list of maps for each shoppingList and its uncheckedItemsAmount
    public Map<String, Integer> getUncheckedItemsAmount() {
        List<ShoppingList> allShoppingLists = shoppingListRepository.findAll();
        Map<String, Integer> amountList = new HashMap<>();

        for (ShoppingList list : allShoppingLists) {
            List<String> itemIds = list.getItemIds();
            int amount = 0;

            for (String id : itemIds) {
                if (!shoppingItemService.getItemById(id).isChecked()) {
                    amount++;
                }
            }
            amountList.put(list.getId(), amount);
        }
        return amountList;
    }

    /*
    ITEM FUNCTIONS:
     */

    // save item in item repo and update item list in shopping list
    public ShoppingItem addOneItemToShoppingList(String header, String listId, ShoppingItem shoppingItem) {
        ShoppingItem createdOrUpdatedItem;
        ShoppingList shoppingList = shoppingListRepository.findById(listId)
                .orElseThrow(() -> new NoSuchElementException("Shopping list not found"));


        if (!shoppingList.getItemIds().contains(shoppingItem.getId())) {
            // create new item
            createdOrUpdatedItem = shoppingItemService.createItem(header, shoppingItem);
            shoppingList.getItemIds().add(createdOrUpdatedItem.getId());
            shoppingListRepository.save(shoppingList);
        } else {
            // add one item amount
            if (shoppingItem.isChecked()) {
                shoppingItem.setChecked(false);
            } else {
                shoppingItem.setAmount(shoppingItem.getAmount() + 1);
            }
            createdOrUpdatedItem = shoppingItemService.updateItem(header, shoppingItem);
        }
        return createdOrUpdatedItem;
    }

    public void deleteItemById(String listId, String itemId) {
        shoppingItemService.deleteItemById(itemId);
        ShoppingList shoppingList = shoppingListRepository.findById(listId)
                .orElseThrow(() -> new NoSuchElementException("Shopping list not found"));
        shoppingList.getItemIds().remove(itemId);
        shoppingListRepository.save(shoppingList);
    }

}

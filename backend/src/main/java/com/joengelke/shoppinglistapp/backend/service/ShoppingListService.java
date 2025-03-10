package com.joengelke.shoppinglistapp.backend.service;

import com.joengelke.shoppinglistapp.backend.model.ShoppingItem;
import com.joengelke.shoppinglistapp.backend.model.ShoppingList;
import com.joengelke.shoppinglistapp.backend.repository.ShoppingListRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ShoppingListService {

    // TODO add WebSockets

    private final ShoppingListRepository shoppingListRepository;
    private final ShoppingItemService shoppingItemService;

    public ShoppingListService(ShoppingListRepository shoppingListRepository, ShoppingItemService shoppingItemService) {
        this.shoppingListRepository = shoppingListRepository;
        this.shoppingItemService = shoppingItemService;
    }

    public ShoppingList createShoppingList(ShoppingList shoppingList) {
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
        shoppingList.setName(newShoppingList.getName());

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
    public List<ShoppingItem> getItemsByShoppingList(String listId) {
        ShoppingList shoppingList = shoppingListRepository.findById(listId)
                .orElseThrow(() -> new NoSuchElementException("Shopping list not found"));

        List<ShoppingItem> itemList = new ArrayList<>();
        for (String itemId : shoppingList.getItemIds()) {
            itemList.add(shoppingItemService.getItemById(itemId));
        }
        return itemList;
    }

    // save item in item repo and update item list in shopping list
    public ShoppingItem addItemToShoppingList(String listId, ShoppingItem shoppingItem) {
        ShoppingItem createdItem = shoppingItemService.createItem(shoppingItem);
        ShoppingList shoppingList = shoppingListRepository.findById(listId)
                .orElseThrow(() -> new NoSuchElementException("Shopping list not found"));

        // add item listId to shopping list's item list
        shoppingList.getItemIds().add(createdItem.getId());

        //save updated shopping list and return created item
        shoppingListRepository.save(shoppingList);
        return createdItem;
    }

    public void deleteItemById(String listId, String itemId) {
        shoppingItemService.deleteItemById(itemId);
        ShoppingList shoppingList = shoppingListRepository.findById(listId)
                .orElseThrow(() -> new NoSuchElementException("Shopping list not found"));
        shoppingList.getItemIds().remove(itemId);
        shoppingListRepository.save(shoppingList);
    }
}

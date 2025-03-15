package com.joengelke.shoppinglistapp.backend.controller;

import com.joengelke.shoppinglistapp.backend.model.ShoppingItem;
import com.joengelke.shoppinglistapp.backend.model.ShoppingList;
import com.joengelke.shoppinglistapp.backend.service.ShoppingListService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shoppinglist")
public class ShoppingListController {

    private final ShoppingListService shoppingListService;

    public ShoppingListController(ShoppingListService shoppingListService) {
        this.shoppingListService = shoppingListService;
    }

    @PostMapping
    public ResponseEntity<?> createShoppingList(@RequestBody ShoppingList shoppingList) {
        ShoppingList savedShoppingList = shoppingListService.createShoppingList(shoppingList);
        return ResponseEntity.ok(Map.of("name", savedShoppingList.getName(), "createdAt", savedShoppingList.getCreatedAt()));
    }

    @GetMapping("s")
    public ResponseEntity<?> getAllShoppingLists() {
        List<ShoppingList> shoppingLists = shoppingListService.getAllShoppingLists();
        return ResponseEntity.ok(shoppingLists);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getShoppingListById(@PathVariable String id) {
        ShoppingList shoppingList = shoppingListService.getShoppingListById(id);
        return ResponseEntity.ok(shoppingList);
    }

    @PutMapping
    public ResponseEntity<?> updateShoppingList(@RequestBody ShoppingList shoppingList) {
        ShoppingList updatedList = shoppingListService.updateShoppingList(shoppingList);
        return ResponseEntity.ok(Map.of("name", updatedList.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteShoppingList(@PathVariable String id) {
        shoppingListService.deleteShoppingList(id);
        return ResponseEntity.ok(Map.of("message","Shopping list deleted successfully"));
    }


    // ItemList changes
    @GetMapping("/{id}/itemIds")
    public ResponseEntity<?> getItemsByShoppingList(@PathVariable String id) {
        List<ShoppingItem> itemList = shoppingListService.getItemsByShoppingList(id);
        return ResponseEntity.ok(itemList);
    }

    @PutMapping("/{id}/item")
    public ResponseEntity<?> addItemToShoppingList(@PathVariable String id, @RequestBody ShoppingItem shoppingItem) {
        ShoppingItem newItem = shoppingListService.addItemToShoppingList(id, shoppingItem);
        //TODO add username of creator maybe from token?!
        return ResponseEntity.ok(newItem);
    }

    @DeleteMapping("/{listId}/item/{itemId}")
    public ResponseEntity<?> deleteItem(@PathVariable String listId, @PathVariable String itemId) {
        shoppingListService.deleteItemById(listId, itemId);
        return ResponseEntity.ok(Map.of("message","Item deleted successfully"));
    }

}

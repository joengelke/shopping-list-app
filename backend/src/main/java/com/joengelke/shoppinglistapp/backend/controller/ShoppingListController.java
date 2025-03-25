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

    @GetMapping
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

    @DeleteMapping("/{shoppingListId}")
    public ResponseEntity<?> deleteShoppingList(@PathVariable String shoppingListId) {
        shoppingListService.deleteShoppingList(shoppingListId);
        return ResponseEntity.ok(Map.of("message", "Shopping list deleted successfully"));
    }


    // ItemList changes
    @GetMapping("/{shoppingListId}/items")
    public ResponseEntity<?> getItemsByShoppingList(@PathVariable String shoppingListId) {
        List<ShoppingItem> itemList = shoppingListService.getItemsByShoppingList(shoppingListId);
        return ResponseEntity.ok(itemList);
    }

    @PutMapping("/{shoppingListId}/item")
    public ResponseEntity<?> addOneItemToShoppingList(
            @PathVariable String shoppingListId,
            @RequestBody ShoppingItem shoppingItem,
            @RequestHeader("Authorization") String header) {
        ShoppingItem newItem = shoppingListService.addOneItemToShoppingList(header, shoppingListId, shoppingItem);
        return ResponseEntity.ok(newItem);
    }

    @PatchMapping("/{shoppingListId}/item/{itemId}")
    public ResponseEntity<?> removeOneItemById(@PathVariable String shoppingListId, @PathVariable String itemId) {
        ShoppingItem updatedItem = shoppingListService.removeOneItemById(shoppingListId, itemId);
        if (updatedItem == null) {
            ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(updatedItem);
    }

    @DeleteMapping("/{shoppingListId}/item/{itemId}")
    public ResponseEntity<?> deleteItemById(@PathVariable String shoppingListId, @PathVariable String itemId) {
        shoppingListService.deleteItemById(shoppingListId, itemId);
        return ResponseEntity.ok(Map.of("message", "Item deleted successfully"));
    }

}

package com.joengelke.shoppinglistapp.backend.controller;

import com.joengelke.shoppinglistapp.backend.model.ItemSet;
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
        return ResponseEntity.ok(savedShoppingList);
    }

    @GetMapping("")
    public ResponseEntity<?> getAllShoppingLists() {
        List<ShoppingList> shoppingLists = shoppingListService.getAllShoppingLists();
        return ResponseEntity.ok(shoppingLists);
    }

    @GetMapping("/uncheckedItemsAmount")
    public ResponseEntity<?> getUncheckedItemsAmount() {
        Map<String, Integer> uncheckedItemsAmount = shoppingListService.getUncheckedItemsAmount();
        return ResponseEntity.ok(uncheckedItemsAmount);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getShoppingListById(@PathVariable String id) {
        ShoppingList shoppingList = shoppingListService.getShoppingListById(id);
        return ResponseEntity.ok(shoppingList);
    }

    @PutMapping
    public ResponseEntity<?> updateShoppingList(@RequestBody ShoppingList shoppingList) {
        ShoppingList updatedShoppingList = shoppingListService.updateShoppingList(shoppingList);
        return ResponseEntity.ok(updatedShoppingList);
    }

    @DeleteMapping("/{shoppingListId}")
    public ResponseEntity<?> deleteShoppingList(@PathVariable String shoppingListId) {
        shoppingListService.deleteShoppingList(shoppingListId);
        return ResponseEntity.ok(Map.of("message", "Shopping list deleted successfully"));
    }

    /*
    ITEMLIST CHANGES
     */

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

    @DeleteMapping("/{shoppingListId}/item/{itemId}")
    public ResponseEntity<?> deleteItemById(@PathVariable String shoppingListId, @PathVariable String itemId) {
        shoppingListService.deleteItemById(shoppingListId, itemId);
        return ResponseEntity.ok(Map.of("message", "Item deleted successfully"));
    }

    /*
    ITEM SET CHANGES
     */

    @GetMapping("/{shoppingListId}/itemsets")
    public ResponseEntity<?> getItemSetsByShoppingList(@PathVariable String shoppingListId) {
        List<ItemSet> itemSets = shoppingListService.getItemSetsByShoppingList(shoppingListId);
        return ResponseEntity.ok(itemSets);
    }

    @PostMapping("/{shoppingListId}/itemset")
    public ResponseEntity<?> createItemSet(
            @PathVariable String shoppingListId,
            @RequestBody ItemSet itemSet,
            @RequestHeader("Authorization") String header) {
        ItemSet newItemSet = shoppingListService.createItemSet(header, shoppingListId, itemSet);
        return ResponseEntity.ok(newItemSet);
    }

    @PutMapping("/{shoppingListId}/itemset")
    public ResponseEntity<?> updateItemSet(
            @PathVariable String shoppingListId,
            @RequestBody ItemSet itemSet,
            @RequestHeader("Authorization") String header) {
        ItemSet updatedItemSet = shoppingListService.updateItemSet(header, shoppingListId, itemSet);
        return ResponseEntity.ok(updatedItemSet);
    }

    @DeleteMapping("/{shoppingListId}/itemset/{itemSetId}")
    public ResponseEntity<?> deleteItemSetById(@PathVariable String shoppingListId, @PathVariable String itemSetId) {
        shoppingListService.deleteItemSetById(shoppingListId, itemSetId);
        return ResponseEntity.ok(Map.of("message", "Item set deleted successfully"));
    }
}

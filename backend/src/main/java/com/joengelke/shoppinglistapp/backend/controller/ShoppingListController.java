package com.joengelke.shoppinglistapp.backend.controller;

import com.joengelke.shoppinglistapp.backend.dto.UserResponse;
import com.joengelke.shoppinglistapp.backend.model.ItemSet;
import com.joengelke.shoppinglistapp.backend.model.ShoppingItem;
import com.joengelke.shoppinglistapp.backend.model.ShoppingList;
import com.joengelke.shoppinglistapp.backend.model.User;
import com.joengelke.shoppinglistapp.backend.service.ShoppingListService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

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
    public ResponseEntity<?> createShoppingList(@RequestBody ShoppingList shoppingList, @RequestHeader("Authorization") String header) {
        ShoppingList savedShoppingList = shoppingListService.createShoppingList(shoppingList, header);
        return ResponseEntity.ok(savedShoppingList);
    }

    @GetMapping("")
    public ResponseEntity<?> getShoppingListsByUserId(@RequestHeader("Authorization") String header) {
        List<ShoppingList> shoppingLists = shoppingListService.getShoppingListsByUserId(header);
        return ResponseEntity.ok(shoppingLists);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllShoppingLists() {
        List<ShoppingList> shoppingLists = shoppingListService.getAllShoppingLists();
        return ResponseEntity.ok(shoppingLists);
    }

    @GetMapping("/uncheckedItemsAmount")
    public ResponseEntity<?> getUncheckedItemsAmount() {
        Map<String, Integer> uncheckedItemsAmount = shoppingListService.getUncheckedItemsAmount();
        return ResponseEntity.ok(uncheckedItemsAmount);
    }

    @PutMapping
    public ResponseEntity<?> updateShoppingList(@RequestBody ShoppingList shoppingList) {
        ShoppingList updatedShoppingList = shoppingListService.updateShoppingList(shoppingList);
        return ResponseEntity.ok(updatedShoppingList);
    }

    @GetMapping("/{shoppingListId}/user")
    public ResponseEntity<?> getShoppingListUser(@PathVariable String shoppingListId) {
        List<UserResponse> users = shoppingListService.getShoppingListUser(shoppingListId);
        return ResponseEntity.ok(users);
    }

    @PostMapping("/{shoppingListId}/user")
    public ResponseEntity<?> addUserToShoppingList(@PathVariable String shoppingListId, @RequestBody User user) {
        try {
            UserResponse addedUser = shoppingListService.addUserToShoppingList(shoppingListId, user.getUsername());
            return ResponseEntity.ok(addedUser);
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/{shoppingListId}/user/{userId}")
    public ResponseEntity<?> removeUserFromShoppingList(@PathVariable String shoppingListId, @PathVariable String userId) {
        shoppingListService.removeUserFromShoppingList(shoppingListId, userId);
        return ResponseEntity.ok(Map.of("message", "User removed successfully"));
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
            @RequestPart("itemSet") ItemSet itemSet,
            @RequestPart(value= "receiptFile", required = false) MultipartFile receiptFile,
            @RequestHeader("Authorization") String header) {
        try {
            ItemSet newItemSet = shoppingListService.createItemSet(header, shoppingListId, itemSet, receiptFile);
            return ResponseEntity.ok(newItemSet);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason()); // code 409 if itemSet already exists
        }
    }

    @PutMapping("/{shoppingListId}/itemset")
    public ResponseEntity<?> updateItemSet(
            @PathVariable String shoppingListId,
            @RequestPart("itemSet") ItemSet itemSet,
            @RequestPart(value= "receiptFile", required = false) MultipartFile receiptFile,
            @RequestHeader("Authorization") String header) {
        ItemSet updatedItemSet = shoppingListService.updateItemSet(header, shoppingListId, itemSet, receiptFile);
        return ResponseEntity.ok(updatedItemSet);
    }

    @DeleteMapping("/{shoppingListId}/itemset/{itemSetId}")
    public ResponseEntity<?> deleteItemSetById(@PathVariable String shoppingListId, @PathVariable String itemSetId) {
        shoppingListService.deleteItemSetById(shoppingListId, itemSetId);
        return ResponseEntity.ok(Map.of("message", "Item set deleted successfully"));
    }

    /*
    USER CHANGES
     */

    @DeleteMapping("/user/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable String userId) {
        shoppingListService.removeUserFromAllShoppingLists(userId);
        return ResponseEntity.ok(Map.of("message", "User successfully deleted!"));
    }
}

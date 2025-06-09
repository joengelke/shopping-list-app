package com.joengelke.shoppinglistapp.backend.controller;

import com.joengelke.shoppinglistapp.backend.model.ItemSetItem;
import com.joengelke.shoppinglistapp.backend.model.ShoppingItem;
import com.joengelke.shoppinglistapp.backend.service.ShoppingItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shoppingitem")
public class ShoppingItemController {

    private final ShoppingItemService shoppingItemService;

    public ShoppingItemController(ShoppingItemService shoppingItemService) {
        this.shoppingItemService = shoppingItemService;
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<?> removeOneItemById(@PathVariable String itemId) {
        ShoppingItem updatedItem = shoppingItemService.removeOneItemById(itemId);
        return ResponseEntity.ok(updatedItem);
    }

    @PutMapping
    public ResponseEntity<?> updateShoppingItem(@RequestBody ShoppingItem shoppingItem, @RequestHeader("Authorization") String header) {
        ShoppingItem updatedItem = shoppingItemService.updateItem(header, shoppingItem);
        return ResponseEntity.ok(updatedItem);
    }

    @PatchMapping("/{itemId}/checked")
    public ResponseEntity<?> updateCheckedStatus(@PathVariable String shoppingListId, @PathVariable String itemId, @RequestParam boolean checked) {
        ShoppingItem shoppingItem = shoppingItemService.updateCheckedStatus(shoppingListId, itemId, checked);
        return ResponseEntity.ok(shoppingItem);
    }

    /*
    ITEM SET METHODS
     */

    @PutMapping("/addItemSetItem")
    public ResponseEntity<?> addItemSetItemToShoppingList(@RequestBody ItemSetItem itemSetItem, @RequestHeader("Authorization") String header) {
        ShoppingItem shoppingItem = shoppingItemService.addItemSetItemToShoppingList(header, itemSetItem);
        return ResponseEntity.ok(shoppingItem);
    }

    @PutMapping("/addAllItemSetItems/{itemSetId}")
    public ResponseEntity<?> addAllItemSetItemsToShoppingList(@PathVariable String itemSetId, @RequestHeader("Authorization") String header) {
        List<ShoppingItem> shoppingItems = shoppingItemService.addAllItemSetItemsToShoppingList(header, itemSetId);
        return ResponseEntity.ok(shoppingItems);
    }

    @PutMapping("/removeItemSetItem")
    public ResponseEntity<?> removeItemSetItemFromShoppingList(@RequestBody ItemSetItem itemSetItem, @RequestHeader("Authorization") String header) {
        ShoppingItem shoppingItem = shoppingItemService.removeItemSetItemFromShoppingList(header, itemSetItem);
        return ResponseEntity.ok(shoppingItem);
    }

    @PutMapping("/removeAllItemSetItems/{itemSetId}")
    public ResponseEntity<?> removeAllItemSetItemsFromShoppingList(@PathVariable String itemSetId, @RequestHeader("Authorization") String header) {
        List<ShoppingItem> shoppingItems = shoppingItemService.removeAllItemSetItemsFromShoppingList(header, itemSetId);
        return ResponseEntity.ok(shoppingItems);
    }
}

package com.joengelke.shoppinglistapp.backend.controller;

import com.joengelke.shoppinglistapp.backend.model.ShoppingItem;
import com.joengelke.shoppinglistapp.backend.service.ShoppingItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shoppingitem")
public class ShoppingItemController {

    private final ShoppingItemService shoppingItemService;

    public ShoppingItemController(ShoppingItemService shoppingItemService) {
        this.shoppingItemService = shoppingItemService;
    }

    @PutMapping
    public ResponseEntity<?> updateShoppingItem(@RequestBody ShoppingItem shoppingItem, @RequestHeader("Authorization") String header) {
        ShoppingItem updatedItem = shoppingItemService.updateItem(header, shoppingItem);
        return ResponseEntity.ok(updatedItem);
    }

    @PatchMapping("/{id}/checked")
    public ResponseEntity<?> updateCheckedStatus(@PathVariable String id, @RequestParam boolean checked) {
        ShoppingItem shoppingItem = shoppingItemService.updateCheckedStatus(id, checked);
        return ResponseEntity.ok(shoppingItem);
    }

    // delete and create item in ShoppingListController
}

package com.joengelke.shoppinglistapp.backend.service;

import com.joengelke.shoppinglistapp.backend.model.ItemSetItem;
import com.joengelke.shoppinglistapp.backend.model.ShoppingItem;
import com.joengelke.shoppinglistapp.backend.model.ShoppingItemActivity;
import com.joengelke.shoppinglistapp.backend.repository.ShoppingItemRepository;
import com.joengelke.shoppinglistapp.backend.security.JwtTokenProvider;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

@Service
public class ShoppingItemService {

    private final ShoppingItemRepository shoppingItemRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final ItemSetService itemSetService;
    private final AnalyticsService analyticsService;

    public ShoppingItemService(ShoppingItemRepository shoppingItemRepository, JwtTokenProvider jwtTokenProvider, ItemSetService itemSetService, AnalyticsService analyticsService) {
        this.shoppingItemRepository = shoppingItemRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.itemSetService = itemSetService;
        this.analyticsService = analyticsService;
    }

    // creates one new item
    public ShoppingItem createItem(String header, ShoppingItem shoppingItem, boolean checked) {

        String username = jwtTokenProvider.getUsernameFromToken(header.replace("Bearer ", ""));

        if (Objects.equals(shoppingItem.getId(), "")) {
            shoppingItem.setId(null);
        }
        if (shoppingItem.getName() == null) {
            shoppingItem.setName("");
        }
        if (shoppingItem.getTags() == null) {
            shoppingItem.setTags(new ArrayList<>());
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

        shoppingItem.setChecked(checked);

        if(!checked) {
           shoppingItem.setCheckedAt(Instant.now());
        }

        shoppingItem.setEditedAt(Instant.now());

        shoppingItem.setEditedBy(username);

        return shoppingItemRepository.save(shoppingItem);
    }

    public ShoppingItem getItemById(String id) {
        return shoppingItemRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Item not found with itemId: " + id));
    }

    public List<ShoppingItem> getAllItemsByIds(List<String> ids) {
        return shoppingItemRepository.findAllById(ids)
                .stream()
                .map(this::updateDB)
                .toList();
    }

    public ShoppingItem updateItem(String header, ShoppingItem newShoppingItem) {
        ShoppingItem shoppingItem = shoppingItemRepository.findById(newShoppingItem.getId())
                .orElseThrow(() -> new NoSuchElementException("Item not found with itemId: " + newShoppingItem.getId()));

        String username = jwtTokenProvider.getUsernameFromToken(header.replace("Bearer ", ""));

        // if the amount or checked status changed then the editedBy and checked at will be changed
        if((!Objects.equals(newShoppingItem.getAmount(), shoppingItem.getAmount())) || (!newShoppingItem.isChecked() && shoppingItem.isChecked())) {
            shoppingItem.setEditedBy(username);
            shoppingItem.setCheckedAt(Instant.now());
        }

        // checked status and createdBy cant be updated
        if (newShoppingItem.getName() != null) {
            shoppingItem.setName(newShoppingItem.getName());
        }
        if (newShoppingItem.getTags() != null) {
            shoppingItem.setTags(newShoppingItem.getTags());
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

        return shoppingItemRepository.save(shoppingItem);
    }

    // just change checked status for better performance
    public ShoppingItem updateCheckedStatus(String shoppingListId, String itemId, boolean checked, String header) {
        ShoppingItem shoppingItem = shoppingItemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Item not found with itemId: " + itemId));

        String userId = jwtTokenProvider.getUserIdFromToken(header.replace("Bearer ", ""));

        shoppingItem.setChecked(checked);
        if(!checked) {
            shoppingItem.setCheckedAt(Instant.now());
        } else {
            analyticsService.addItemAnalyticsEvent(shoppingListId, userId, shoppingItem, "checked");
            shoppingItem.setAmount(0.0);
            shoppingItem.setUnit("");
        }
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
        } else {
            // checks item so its not on active list anymore
            shoppingItem.setChecked(true);
        }
        return shoppingItemRepository.save(shoppingItem);
    }

    public void deleteItemById(String id) {
        shoppingItemRepository.deleteById(id);
    }

    /*
    ITEM SET METHODS
     */

    public ShoppingItem addItemSetItemToShoppingList(String header, ItemSetItem itemSetItem) {
        ShoppingItem shoppingItem = shoppingItemRepository.findById(itemSetItem.getId())
                .orElseThrow(() -> new NoSuchElementException("Item not found with itemId: " + itemSetItem.getId()));
        String username = jwtTokenProvider.getUsernameFromToken(header.replace("Bearer ", ""));

        //update shoppingItem by itemSetItem values
        shoppingItem.setAmount(shoppingItem.getAmount() + itemSetItem.getAmount());
        shoppingItem.setUnit(itemSetItem.getUnit());
        shoppingItem.setChecked(false);
        shoppingItem.setEditedAt(Instant.now());
        shoppingItem.setEditedBy(username);

        return shoppingItemRepository.save(shoppingItem);
    }

    public List<ShoppingItem> addAllItemSetItemsToShoppingList(String header, String itemSetId) {
        String username = jwtTokenProvider.getUsernameFromToken(header.replace("Bearer ", ""));

        List<ItemSetItem> itemSetItems = itemSetService.getItemSetItemsById(itemSetId);

        List<ShoppingItem> updatedItems = new ArrayList<>();

        for (ItemSetItem itemSetItem : itemSetItems) {
            //update shoppingItems by itemSetItem values

            ShoppingItem shoppingItem = shoppingItemRepository.findById(itemSetItem.getId())
                    .orElseThrow(() -> new NoSuchElementException("Item not found with itemId: " + itemSetItem.getId()));

            shoppingItem.setAmount(shoppingItem.getAmount() + itemSetItem.getAmount());
            //shoppingItem.setUnit(itemSetItem.getUnit());
            shoppingItem.setChecked(false);
            shoppingItem.setEditedAt(Instant.now());
            shoppingItem.setEditedBy(username);

            updatedItems.add(shoppingItem);
        }

        return shoppingItemRepository.saveAll(updatedItems);
    }

    public ShoppingItem removeItemSetItemFromShoppingList(String header, ItemSetItem itemSetItem) {
        ShoppingItem shoppingItem = shoppingItemRepository.findById(itemSetItem.getId())
                .orElseThrow(() -> new NoSuchElementException("Item not found with itemId: " + itemSetItem.getId()));
        String username = jwtTokenProvider.getUsernameFromToken(header.replace("Bearer ", ""));

        //update shoppingItem by itemSetItem values
        shoppingItem.setAmount(shoppingItem.getAmount() - itemSetItem.getAmount());
        //shoppingItem.setUnit(itemSetItem.getUnit());
        shoppingItem.setChecked(shoppingItem.getAmount() == 0);
        shoppingItem.setEditedAt(Instant.now());
        shoppingItem.setEditedBy(username);

        return shoppingItemRepository.save(shoppingItem);
    }

    public List<ShoppingItem> removeAllItemSetItemsFromShoppingList(String header, String itemSetId) {
        String username = jwtTokenProvider.getUsernameFromToken(header.replace("Bearer ", ""));

        List<ItemSetItem> itemSetItems = itemSetService.getItemSetItemsById(itemSetId);

        List<ShoppingItem> updatedItems = new ArrayList<>();

        for (ItemSetItem itemSetItem : itemSetItems) {
            //update shoppingItems by itemSetItem values

            ShoppingItem shoppingItem = shoppingItemRepository.findById(itemSetItem.getId())
                    .orElseThrow(() -> new NoSuchElementException("Item not found with itemId: " + itemSetItem.getId()));

            shoppingItem.setAmount(shoppingItem.getAmount() - itemSetItem.getAmount());
            //shoppingItem.setUnit(itemSetItem.getUnit());
            shoppingItem.setChecked(false);
            shoppingItem.setEditedAt(Instant.now());
            shoppingItem.setEditedBy(username);

            updatedItems.add(shoppingItem);
        }

        return shoppingItemRepository.saveAll(updatedItems);
    }

    // HELP METHOD TO UPDATE DB AFTER ITEM CHANGE
    private ShoppingItem updateDB(ShoppingItem item) {
        // updates new tags variable
        if(item.getTags() == null) {
            item.setTags(List.of());
        }
        return item;
    }
}

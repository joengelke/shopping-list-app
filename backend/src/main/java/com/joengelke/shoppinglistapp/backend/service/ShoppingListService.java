package com.joengelke.shoppinglistapp.backend.service;

import com.joengelke.shoppinglistapp.backend.dto.UserResponse;
import com.joengelke.shoppinglistapp.backend.model.*;
import com.joengelke.shoppinglistapp.backend.repository.ShoppingListRepository;
import com.joengelke.shoppinglistapp.backend.security.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ShoppingListService {

    private final ShoppingListRepository shoppingListRepository;
    private final ShoppingItemService shoppingItemService;
    private final ItemSetService itemSetService;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    public ShoppingListService(ShoppingListRepository shoppingListRepository, ShoppingItemService shoppingItemService, ItemSetService itemSetService, UserService userService, JwtTokenProvider jwtTokenProvider) {
        this.shoppingListRepository = shoppingListRepository;
        this.shoppingItemService = shoppingItemService;
        this.itemSetService = itemSetService;
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public ShoppingList createShoppingList(ShoppingList shoppingList, String header) {

        String username = jwtTokenProvider.getUsernameFromToken(header.replace("Bearer ", ""));
        User user = userService.getUserByUsername(username);

        if (shoppingList.getName() == null) {
            shoppingList.setName("");
        }
        if (shoppingList.getCreatedAt() == null) {
            shoppingList.setCreatedAt(Instant.now());
        }
        if (shoppingList.getItemIds() == null) {
            shoppingList.setItemIds(new ArrayList<>());
        }
        if (shoppingList.getItemSetIds() == null) {
            shoppingList.setItemSetIds(new ArrayList<>());
        }
        if (shoppingList.getUserIds() == null) {
            shoppingList.setUserIds(List.of(user.getId()));
        }
        return shoppingListRepository.save(shoppingList);
    }

    public List<ShoppingList> getAllShoppingLists() {
        return shoppingListRepository.findAll();
    }

    public List<ShoppingList> getShoppingListsByUserId(String header) {
        String username = jwtTokenProvider.getUsernameFromToken(header.replace("Bearer ", ""));
        User user = userService.getUserByUsername(username);

        return shoppingListRepository.findAll().stream()
                .filter(list -> list.getUserIds().contains(user.getId()))
                .collect(Collectors.toList());
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

    public List<UserResponse> getShoppingListUser(String listId) {
        ShoppingList shoppingList = shoppingListRepository.findById(listId)
                .orElseThrow(() -> new NoSuchElementException("Shopping list not found"));

        return userService.getAllUserByIds(shoppingList.getUserIds());
    }

    public UserResponse addUserToShoppingList(String listId, String username) {
        ShoppingList shoppingList = shoppingListRepository.findById(listId)
                .orElseThrow(() -> new NoSuchElementException("Shopping list not found"));

        User user = userService.getUserByUsername(username);

        if (!shoppingList.getUserIds().contains(user.getId())) {
            shoppingList.getUserIds().add(user.getId());
            shoppingListRepository.save(shoppingList);
        }

        return new UserResponse(user);
    }

    public void removeUserFromShoppingList(String listId, String userId) {
        ShoppingList shoppingList = shoppingListRepository.findById(listId)
                .orElseThrow(() -> new NoSuchElementException("Shopping list not found"));

        if (shoppingList.getUserIds().contains(userId)) {
            shoppingList.getUserIds().remove(userId);
            shoppingListRepository.save(shoppingList);
        }
    }

    public void removeUserFromAllShoppingLists(String userId) {
        List<ShoppingList> allLists = shoppingListRepository.findAll();

        for (ShoppingList list : allLists) {
            list.getUserIds().remove(userId);
        }

        shoppingListRepository.saveAll(allLists);
        userService.deleteUser(userId);
    }

    public void deleteShoppingList(String listId) {
        ShoppingList shoppingList = shoppingListRepository.findById(listId)
                .orElseThrow(() -> new NoSuchElementException("Shopping list not found"));

        // delete each item in list
        for (String itemId : shoppingList.getItemIds()) {
            shoppingItemService.deleteItemById(itemId);
        }

        shoppingListRepository.deleteById(listId);
    }

    // returns shoppingItemList
    public List<ShoppingItem> getItemsByShoppingList(String listId) {
        ShoppingList shoppingList = shoppingListRepository.findById(listId)
                .orElseThrow(() -> new NoSuchElementException("Shopping list not found"));

        return shoppingItemService.getAllItemsByIds(shoppingList.getItemIds());
    }

    // return list of maps for each shoppingList and its uncheckedItemsAmount
    public Map<String, Integer> getUncheckedItemsAmount() {
        List<ShoppingList> allShoppingLists = shoppingListRepository.findAll();
        Map<String, Integer> amountList = new HashMap<>();

        for (ShoppingList list : allShoppingLists) {
            List<String> itemIds = list.getItemIds();
            int amount = 0;

            List<ShoppingItem> shoppingItems = shoppingItemService.getAllItemsByIds(itemIds);
            for (ShoppingItem item : shoppingItems) {
                if (!item.isChecked()) {
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
        String username = jwtTokenProvider.getUsernameFromToken(header.replace("Bearer ", ""));

        if (!shoppingList.getItemIds().contains(shoppingItem.getId())) {
            // create new item
            createdOrUpdatedItem = shoppingItemService.createItem(header, shoppingItem, false);
            shoppingList.getItemIds().add(createdOrUpdatedItem.getId());
            shoppingListRepository.save(shoppingList);
        } else {
            // add one item amount
            if (shoppingItem.isChecked()) {
                shoppingItem.setAmount(0.0);
                shoppingItem.setUnit("");
                shoppingItem.setChecked(false);
                shoppingItem.setCheckedAt(Instant.now());
            } else {
                shoppingItem.setAmount(shoppingItem.getAmount() + 1);
                shoppingItem.setCheckedAt(Instant.now());
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

    /*
    ITEMSET FUNCTIONS:
     */

    public List<ItemSet> getItemSetsByShoppingList(String listId) {
        ShoppingList shoppingList = shoppingListRepository.findById(listId)
                .orElseThrow(() -> new NoSuchElementException("Shopping list not found"));

        return itemSetService.getAllItemSetsByIds(shoppingList.getItemSetIds());
    }

    public ItemSet createItemSet(String header, String listId, ItemSet itemSet) {
        ShoppingList shoppingList = shoppingListRepository.findById(listId)
                .orElseThrow(() -> new NoSuchElementException("Shopping list not found"));

        // Only check names of ItemSets referenced by this shoppingList
        List<String> itemSetIds = shoppingList.getItemSetIds();
        if (itemSetIds != null && !itemSetIds.isEmpty()) {
            List<ItemSet> existingSets = itemSetService.getAllItemSetsByIds(itemSetIds);
            boolean nameExists = existingSets.stream()
                    .anyMatch(existingSet -> existingSet.getName().equalsIgnoreCase(itemSet.getName()));

            if (nameExists) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "An ItemSet with that name already exists in this shopping list."
                );
            }
        }

        // creates or matches new shoppingItems from itemSetItems
        for (ItemSetItem itemSetItem : itemSet.getItemList()) {
            Optional<ShoppingItem> matchingItem = getItemsByShoppingList(listId).stream()
                    .filter(item -> item.getName().equals(itemSetItem.getName()))
                    .findFirst();
            if (matchingItem.isPresent()) {
                // found shoppingItem with matching name -> update id of itemSetItem
                itemSetItem.setId(matchingItem.get().getId());
            } else {
                // no shoppingItem with matching name exists -> create new item and update id of itemSetItem
                ShoppingItem newShoppingItem = shoppingItemService.createItem(
                        header,
                        new ShoppingItem(
                                itemSetItem.getName(),
                                Collections.emptyList(),
                                itemSetItem.getAmount(),
                                itemSetItem.getUnit(),
                                "",
                                ""),
                        true
                );
                itemSetItem.setId(newShoppingItem.getId());

                shoppingList.getItemIds().add(newShoppingItem.getId());
            }
        }

        // creates new ItemSet
        ItemSet newItemSet = itemSetService.createItemSet(new ItemSet(itemSet.getName(), itemSet.getItemList()));
        if (shoppingList.getItemSetIds() == null) {
            shoppingList.setItemSetIds(new ArrayList<>());
        }
        shoppingList.getItemSetIds().add(newItemSet.getId());
        shoppingListRepository.save(shoppingList);
        return newItemSet;
    }

    public ItemSet updateItemSet(String header, String listId, ItemSet newItemSet) {
        ShoppingList shoppingList = shoppingListRepository.findById(listId)
                .orElseThrow(() -> new NoSuchElementException("Shopping list not found"));

        for (ItemSetItem itemSetItem : newItemSet.getItemList()) {
            if (itemSetItem.getId().isBlank() || !itemSetItem.getName().equals(shoppingItemService.getItemById(itemSetItem.getId()).getName())) {
                // create or match shoppingItem
                Optional<ShoppingItem> matchingItem = getItemsByShoppingList(listId).stream()
                        .filter(item -> item.getName().equals(itemSetItem.getName()))
                        .findFirst();
                if (matchingItem.isPresent()) {
                    // found shoppingItem with matching name -> update id of itemSetItem
                    itemSetItem.setId(matchingItem.get().getId());
                } else {
                    // no shoppingItem with matching name exists -> create new item and update id of itemSetItem
                    ShoppingItem newShoppingItem = shoppingItemService.createItem(
                            header,
                            new ShoppingItem(
                                    itemSetItem.getName(),
                                    Collections.emptyList(),
                                    itemSetItem.getAmount(),
                                    itemSetItem.getUnit(),
                                    "",
                                    ""),
                            true
                    );
                    itemSetItem.setId(newShoppingItem.getId());

                    shoppingList.getItemIds().add(newShoppingItem.getId());
                }
            }
        }

        shoppingListRepository.save(shoppingList);
        return itemSetService.updateItemSet(newItemSet);
    }

    public void deleteItemSetById(String listId, String itemSetId) {
        itemSetService.deleteItemSetById(itemSetId);
        ShoppingList shoppingList = shoppingListRepository.findById(listId)
                .orElseThrow(() -> new NoSuchElementException("Shopping list not found"));
        shoppingList.getItemIds().remove(itemSetId);
        shoppingListRepository.save(shoppingList);
    }
}

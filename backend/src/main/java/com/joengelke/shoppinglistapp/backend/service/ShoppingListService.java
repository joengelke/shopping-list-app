package com.joengelke.shoppinglistapp.backend.service;

import com.joengelke.shoppinglistapp.backend.model.ItemSet;
import com.joengelke.shoppinglistapp.backend.model.ItemSetItem;
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
    private final ItemSetService itemSetService;

    public ShoppingListService(ShoppingListRepository shoppingListRepository, ShoppingItemService shoppingItemService, ItemSetService itemSetService) {
        this.shoppingListRepository = shoppingListRepository;
        this.shoppingItemService = shoppingItemService;
        this.itemSetService = itemSetService;
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

    public ShoppingList getShoppingListById(String listId) {

        return shoppingListRepository.findById(listId)
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


        if (!shoppingList.getItemIds().contains(shoppingItem.getId())) {
            // create new item
            createdOrUpdatedItem = shoppingItemService.createItem(header, shoppingItem, false);
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

    /*
    ITEMSET FUNCTIONS:
     */

    public List<ItemSet> getItemSetsByShoppingList(String listId) {
        ShoppingList shoppingList = shoppingListRepository.findById(listId)
                .orElseThrow(() -> new NoSuchElementException("Shopping list not found"));

        List<ShoppingItem> shoppingItems = shoppingItemService.getAllItemsByIds(shoppingList.getItemIds());
        return itemSetService.getAllItemSetsByIds(shoppingList.getItemSetIds());
    }

    public ItemSet createItemSet(String header, String listId, ItemSet itemSet) {
        ShoppingList shoppingList = shoppingListRepository.findById(listId)
                .orElseThrow(() -> new NoSuchElementException("Shopping list not found"));

        List<ItemSetItem> newItemSetItems = new ArrayList<>(); // final itemSetItem list to use for
        for (ItemSetItem setItem : itemSet.getItemList()) {
            if (!shoppingList.getItemIds().contains(setItem.getId())) {
                //create new shoppingItem, add new ItemSetItem with new shoppingItem id
                ShoppingItem shoppingItem = shoppingItemService.createItem(header, new ShoppingItem(setItem.getName(), "", 0.0, "", "", ""), false);
                shoppingList.getItemIds().add(shoppingItem.getId());
                newItemSetItems.add(new ItemSetItem(shoppingItem.getId(), setItem.getTmpId(), setItem.getName(), setItem.getAmount(), setItem.getUnit()));
            } else {
                //shoppingItem already exists, add new ItemSetItem
                newItemSetItems.add(new ItemSetItem(setItem.getId(), setItem.getTmpId(), setItem.getName(), setItem.getAmount(), setItem.getUnit()));
            }
        }

        ItemSet newItemSet = itemSetService.createItemSet(new ItemSet(itemSet.getName(), newItemSetItems));
        shoppingList.getItemSetIds().add(newItemSet.getId());
        shoppingListRepository.save(shoppingList);
        return newItemSet;
    }

    public ItemSet updateItemSet(String header, String listId, ItemSet newItemSet) {
        ShoppingList shoppingList = shoppingListRepository.findById(listId)
                .orElseThrow(() -> new NoSuchElementException("Shopping list not found"));

        for (ItemSetItem itemSetItem : newItemSet.getItemList()) {
            if (itemSetItem.getId().isBlank()) {
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
                                    "",
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

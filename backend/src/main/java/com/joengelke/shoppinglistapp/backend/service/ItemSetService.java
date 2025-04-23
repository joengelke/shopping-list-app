package com.joengelke.shoppinglistapp.backend.service;

import com.joengelke.shoppinglistapp.backend.model.ItemSet;
import com.joengelke.shoppinglistapp.backend.model.ItemSetItem;
import com.joengelke.shoppinglistapp.backend.repository.ItemSetRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ItemSetService {

    private final ItemSetRepository itemSetRepository;

    public ItemSetService(ItemSetRepository itemSetRepository) {
        this.itemSetRepository = itemSetRepository;
    }

    public List<ItemSet> getAllItemSetsByIds(List<String> itemSetIds) {
        return itemSetRepository.findAllById(itemSetIds);
    }

    public ItemSet createItemSet(ItemSet itemSet) {
        return itemSetRepository.save(itemSet);
    }

    public void deleteItemSetById(String itemSetId) {
        itemSetRepository.deleteById(itemSetId);
    }

    public ItemSet updateItemSet(ItemSet newItemSet) {
        ItemSet itemSet = itemSetRepository.findById(newItemSet.getId())
                .orElseThrow(() -> new NoSuchElementException("Item set not found"));

        itemSet.setName(newItemSet.getName());
        itemSet.setItemList(newItemSet.getItemList());
        return itemSetRepository.save(itemSet);
    }

    public List<ItemSetItem> getItemSetItemsById(String itemSetId) {
        ItemSet itemSet = itemSetRepository.findById((itemSetId))
                .orElseThrow(() -> new NoSuchElementException("Item set not found"));
        return itemSet.getItemList();
    }
}

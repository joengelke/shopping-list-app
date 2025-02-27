package com.joengelke.shoppinglistapp.backend.repository;

import com.joengelke.shoppinglistapp.backend.model.ShoppingItem;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ShoppingItemRepository extends MongoRepository<ShoppingItem, String> {
}

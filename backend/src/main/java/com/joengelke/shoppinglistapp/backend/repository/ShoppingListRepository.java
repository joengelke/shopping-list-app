package com.joengelke.shoppinglistapp.backend.repository;

import com.joengelke.shoppinglistapp.backend.model.ShoppingList;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ShoppingListRepository extends MongoRepository<ShoppingList, String> {
}

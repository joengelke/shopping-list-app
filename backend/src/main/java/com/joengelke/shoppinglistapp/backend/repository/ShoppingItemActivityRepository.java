package com.joengelke.shoppinglistapp.backend.repository;

import com.joengelke.shoppinglistapp.backend.model.ShoppingItemActivity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

public interface ShoppingItemActivityRepository extends MongoRepository<ShoppingItemActivity, String>{
    List<ShoppingItemActivity> findAllByListId(String shoppingListId);

    List<ShoppingItemActivity> findAllByUserId(String userId);

    List<ShoppingItemActivity> findAllByListIdAndUserId(String shoppingListId, String userId);

    List<ShoppingItemActivity> findAllByListIdAndTimestampBetween(String shoppingListId, Instant from, Instant to);

    List<ShoppingItemActivity> findAllByListIdAndUserIdAndTimestampBetween(String shoppingListId, String userId, Instant from, Instant to);
}


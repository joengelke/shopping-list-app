package com.joengelke.shoppinglistapp.backend.repository;

import com.joengelke.shoppinglistapp.backend.model.ShoppingItemActivity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ShoppingItemActivityRepository extends MongoRepository<ShoppingItemActivity, String>{
}


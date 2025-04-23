package com.joengelke.shoppinglistapp.backend.repository;

import com.joengelke.shoppinglistapp.backend.model.ItemSet;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ItemSetRepository extends MongoRepository<ItemSet, String> {
}

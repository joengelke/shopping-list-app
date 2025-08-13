package com.joengelke.shoppinglistapp.backend.repository;

import com.joengelke.shoppinglistapp.backend.model.Recipe;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RecipeRepository extends MongoRepository<Recipe, String> {
    boolean existsByCreatorIdAndItemSetId(String userId, String id);
}


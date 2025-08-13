package com.joengelke.shoppinglistapp.backend.repository;

import com.joengelke.shoppinglistapp.backend.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User,String> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    List<User> findByRolesContaining(String role);

    List<User> findByRecipeIdsContaining(String recipeId);
}

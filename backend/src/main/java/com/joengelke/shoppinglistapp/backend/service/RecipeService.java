package com.joengelke.shoppinglistapp.backend.service;

import com.joengelke.shoppinglistapp.backend.dto.UserResponse;
import com.joengelke.shoppinglistapp.backend.model.ItemSet;
import com.joengelke.shoppinglistapp.backend.model.Recipe;
import com.joengelke.shoppinglistapp.backend.model.Visibility;
import com.joengelke.shoppinglistapp.backend.repository.RecipeRepository;
import com.joengelke.shoppinglistapp.backend.repository.UserRepository;
import com.joengelke.shoppinglistapp.backend.security.JwtTokenProvider;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final MongoTemplate mongoTemplate;

    public RecipeService(RecipeRepository recipeRepository, UserRepository userRepository, UserService userService, JwtTokenProvider jwtTokenProvider, MongoTemplate mongoTemplate) {
        this.recipeRepository = recipeRepository;
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.mongoTemplate = mongoTemplate;
    }

    public List<Recipe> getRecipesByUserId(String header) {
        String userId = jwtTokenProvider.getUserIdFromToken(header.replace("Bearer ", ""));
        List<String> recipeIds = userService.getRecipeIdsByUserId(userId);
        if (recipeIds == null || recipeIds.isEmpty()) {
            return Collections.emptyList();
        }

        return recipeRepository.findAllById(recipeIds).stream().filter(recipe ->
                !recipe.getVisibility().equals(Visibility.PRIVATE) ||
                        recipe.getCreatorId().equals(userId)
        ).toList();
    }

    public List<Recipe> getAllRecipes() {
        return recipeRepository.findAll();
    }

    public List<Recipe> getAllMarketplaceRecipesByUserId(String header) {
        String userId = jwtTokenProvider.getUserIdFromToken(header.replace("Bearer ", ""));

        // Get all receipts where:
        // - user is creator
        // - OR receipt is shared with the user
        // - OR receipt is public
        Criteria criteria = new Criteria().orOperator(
                new Criteria().andOperator(
                        Criteria.where("visibility").is(Visibility.SHARED),
                        Criteria.where("sharedWithUserIds").in(userId)
                ),
                Criteria.where("visibility").is(Visibility.PUBLIC)
        );
        Query query = new Query(criteria);

        return mongoTemplate.find(query, Recipe.class);
    }

    public Recipe createRecipe(String header, Recipe recipe) {
        String userId = jwtTokenProvider.getUserIdFromToken(header.replace("Bearer ", ""));
        Recipe newRecipe = recipeRepository.save(
                new Recipe(
                        recipe.getName(),
                        userId,
                        recipe.getItemSet(),
                        recipe.getDescription(),
                        recipe.getInstructions(),
                        recipe.getCategories(),
                        recipe.getVisibility(),
                        recipe.getSharedWithUserIds(),
                        recipe.getReceiptFileId()
                )
        );
        userService.addRecipeToUser(userId, newRecipe.getId());
        return newRecipe;
    }

    public Recipe convertItemSetToRecipe(String header, ItemSet itemSet) {
        String userId = jwtTokenProvider.getUserIdFromToken(header.replace("Bearer ", ""));
        boolean alreadyExists = recipeRepository.existsByCreatorIdAndItemSetId(userId, itemSet.getId());
        if (alreadyExists) {
            throw new IllegalStateException("This itemSet is already saved as a recipe.");
        }
        Recipe recipe = new Recipe(itemSet.getName(), userId, itemSet, "", new ArrayList<>(), new ArrayList<>(), Visibility.PRIVATE, new ArrayList<>(), "");
        Recipe savedRecipe = recipeRepository.save(recipe);
        userService.addRecipeToUser(userId, savedRecipe.getId());
        return savedRecipe;
    }

    public Recipe changeVisibility(String header, String recipeId, Visibility visibility) {
        String userId = jwtTokenProvider.getUserIdFromToken(header.replace("Bearer ", ""));
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new NoSuchElementException("Recipe not found"));
        if (!recipe.getCreatorId().equals(userId)) {
            throw new AccessDeniedException("You are not allowed to change the visibility");
        }
        recipe.setVisibility(visibility);
        return recipeRepository.save(recipe);
    }

    public Recipe updateRecipe(String header, Recipe recipe) {
        String userId = jwtTokenProvider.getUserIdFromToken(header.replace("Bearer ", ""));
        if (!recipe.getCreatorId().equals(userId)) {
            throw new AccessDeniedException("You are not allowed to delete");
        }
        Recipe existing = recipeRepository.findById(recipe.getId())
                .orElseThrow(() -> new NoSuchElementException("Recipe not found"));

        // Update mutable fields
        existing.setName(recipe.getName());
        existing.setDescription(recipe.getDescription());
        existing.setItemSet(recipe.getItemSet());
        existing.setInstructions(recipe.getInstructions());
        existing.setCategories(recipe.getCategories());
        // existing.setVisibility(recipe.getVisibility());
        // existing.setSharedWithUserIds(recipe.getSharedWithUserIds());
        // existing.setReceiptFileId(recipe.getReceiptFileId());

        return recipeRepository.save(existing);
    }

    public List<Recipe> addRecipeToUser(String header, String recipeId, String username) {
        String userId;

        if (username != null && !username.isBlank()) {
            userId = userService.getUserByUsername(username).getId();
        } else {
            userId = jwtTokenProvider.getUserIdFromToken(header.replace("Bearer ", ""));
        }

        UserResponse user = userService.addRecipeToUser(userId, recipeId);
        return recipeRepository.findAllById(user.getRecipeIds());
    }


    public void removeRecipeFromUser(String header, String recipeId, String userId) {
        String currentUserId = jwtTokenProvider.getUserIdFromToken(header.replace("Bearer ", ""));

        String targetUserId = (userId != null && !userId.isBlank()) ? userId
                : currentUserId;

        // Check permissions: only self or admin removal allowed
        if (!targetUserId.equals(currentUserId) && !jwtTokenProvider.isAdmin(header.replace("Bearer ", ""))) {
            throw new AccessDeniedException("You are not allowed to remove recipes of that user");
        }

        userService.removeRecipeFromUser(targetUserId, recipeId);
    }


    public void deleteRecipe(String header, String recipeId) {
        String userId = jwtTokenProvider.getUserIdFromToken(header.replace("Bearer ", ""));
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new NoSuchElementException("Recipe not found"));
        if (!recipe.getCreatorId().equals(userId) && !jwtTokenProvider.isAdmin(header.replace("Bearer ", ""))) {
            throw new AccessDeniedException("You are not allowed to delete");
        }
        userService.removeRecipeFromAllUsers(recipeId);
        recipeRepository.delete(recipe);
    }
}
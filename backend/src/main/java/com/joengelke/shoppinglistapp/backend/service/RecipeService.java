package com.joengelke.shoppinglistapp.backend.service;

import com.joengelke.shoppinglistapp.backend.dto.FileResourceDTO;
import com.joengelke.shoppinglistapp.backend.dto.UserResponse;
import com.joengelke.shoppinglistapp.backend.model.ItemSet;
import com.joengelke.shoppinglistapp.backend.model.Recipe;
import com.joengelke.shoppinglistapp.backend.model.Visibility;
import com.joengelke.shoppinglistapp.backend.repository.RecipeRepository;
import com.joengelke.shoppinglistapp.backend.repository.UserRepository;
import com.joengelke.shoppinglistapp.backend.security.JwtTokenProvider;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.types.ObjectId;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final MongoTemplate mongoTemplate;
    private final GridFsTemplate gridFsTemplate;

    public RecipeService(RecipeRepository recipeRepository, UserRepository userRepository, UserService userService, JwtTokenProvider jwtTokenProvider, MongoTemplate mongoTemplate, GridFsTemplate gridFsTemplate) {
        this.recipeRepository = recipeRepository;
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.mongoTemplate = mongoTemplate;
        this.gridFsTemplate = gridFsTemplate;
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
        String username = jwtTokenProvider.getUsernameFromToken(header.replace("Bearer ", ""));
        Recipe newRecipe = recipeRepository.save(
                new Recipe(
                        recipe.getName(),
                        userId,
                        username,
                        recipe.getItemSet(),
                        recipe.getDescription(),
                        recipe.getInstructions(),
                        recipe.getCategories(),
                        recipe.getVisibility(),
                        recipe.getSharedWithUserIds(),
                        recipe.getRecipeFileIds()
                )
        );
        userService.addRecipeToUser(userId, newRecipe.getId());
        return newRecipe;
    }

    public Recipe convertItemSetToRecipe(String header, ItemSet itemSet) {
        String userId = jwtTokenProvider.getUserIdFromToken(header.replace("Bearer ", ""));
        String username = jwtTokenProvider.getUsernameFromToken(header.replace("Bearer ", ""));
        boolean alreadyExists = recipeRepository.existsByCreatorIdAndItemSetId(userId, itemSet.getId());
        if (alreadyExists) {
            throw new IllegalStateException("This itemSet is already saved as a recipe.");
        }
        Recipe recipe = new Recipe(itemSet.getName(), userId, username, itemSet, "", new ArrayList<>(), new ArrayList<>(), Visibility.PRIVATE, new ArrayList<>(), new ArrayList<>());
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

    public Recipe updateRecipe(String header, Recipe newRecipe, List<MultipartFile> recipeFiles) {
        String userId = jwtTokenProvider.getUserIdFromToken(header.replace("Bearer ", ""));
        if (!newRecipe.getCreatorId().equals(userId)) {
            throw new AccessDeniedException("You are not allowed to update");
        }
        Recipe existingRecipe = recipeRepository.findById(newRecipe.getId())
                .orElseThrow(() -> new NoSuchElementException("Recipe not found"));

        // Update mutable fields
        existingRecipe.setName(newRecipe.getName());
        existingRecipe.setDescription(newRecipe.getDescription());
        existingRecipe.setItemSet(newRecipe.getItemSet());
        existingRecipe.setInstructions(newRecipe.getInstructions());
        existingRecipe.setCategories(newRecipe.getCategories());
        // existing.setVisibility(recipe.getVisibility());
        // existing.setSharedWithUserIds(recipe.getSharedWithUserIds());

        List<String> oldFileIds = existingRecipe.getRecipeFileIds();
        List<String> newFileIds = newRecipe.getRecipeFileIds();

        // Find files that were removed in the update
        List<String> removedFileIds = oldFileIds.stream()
                .filter(oldId -> !newFileIds.contains(oldId))
                .toList();

        if (!removedFileIds.isEmpty()) {
            gridFsTemplate.delete(
                    Query.query(Criteria.where("_id").in(removedFileIds))
            );
        }

        // Add new uploaded files (if recipeFiles contains fresh files)
        if (recipeFiles != null && !recipeFiles.isEmpty()) {
            for (MultipartFile file : recipeFiles) {
                try {
                    ObjectId fileId = gridFsTemplate.store(
                            file.getInputStream(),
                            file.getOriginalFilename(),
                            file.getContentType()
                    );
                    newFileIds.add(fileId.toHexString());
                }  catch (IOException e) {
                    throw new RuntimeException("Failed to store file: " + file.getOriginalFilename(), e);
                }
            }
        }
        existingRecipe.setRecipeFileIds(newFileIds);

        return recipeRepository.save(existingRecipe);
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

    public FileResourceDTO getRecipeFile(String recipeId, String recipeFileId) throws IOException {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("Recipe not found"));

        List<String> recipeFileIds = recipe.getRecipeFileIds();
        if (recipeFileIds == null) {
            throw new RuntimeException("No files in this recipe");
        }

        if (!recipeFileIds.contains(recipeFileId)) {
            throw new RuntimeException("File ID does not belong to this recipe");
        }

        ObjectId fileId = new ObjectId(recipeFileId);
        GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(fileId)));

        if (gridFSFile == null) {
            throw new RuntimeException("File not found for id: " + recipeFileId);
        }

        GridFsResource resource = gridFsTemplate.getResource(gridFSFile);

        String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        if (gridFSFile.getMetadata() != null && gridFSFile.getMetadata().getString("_contentType") != null) {
            contentType = gridFSFile.getMetadata().getString("_contentType");
        }

        return new FileResourceDTO(
                new InputStreamResource(resource.getInputStream()),
                contentType,
                gridFSFile.getFilename()
        );
    }

    public List<String> getRecipeCategoriesByPopularity() {
        // first category is most popular by number, last is the least popular
        List<Recipe> allRecipes = recipeRepository.findAll();
        return allRecipes.stream()
                .flatMap(recipe -> recipe.getCategories().stream())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
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
        if (recipe.getRecipeFileIds() != null && !recipe.getRecipeFileIds().isEmpty()) {
            gridFsTemplate.delete(
                    Query.query(Criteria.where("_id").in(recipe.getRecipeFileIds()))
            );
        }
        userService.removeRecipeFromAllUsers(recipeId);
        recipeRepository.delete(recipe);
    }
}
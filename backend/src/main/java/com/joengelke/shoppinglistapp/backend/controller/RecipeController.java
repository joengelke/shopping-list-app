package com.joengelke.shoppinglistapp.backend.controller;

import com.joengelke.shoppinglistapp.backend.dto.FileResourceDTO;
import com.joengelke.shoppinglistapp.backend.model.ItemSet;
import com.joengelke.shoppinglistapp.backend.model.Recipe;
import com.joengelke.shoppinglistapp.backend.model.Visibility;
import com.joengelke.shoppinglistapp.backend.service.RecipeService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recipe")
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @GetMapping
    public ResponseEntity<?> getRecipesByUserId(
            @RequestHeader("Authorization") String header
    ) {
        List<Recipe> recipeList = recipeService.getRecipesByUserId(header);
        return ResponseEntity.ok(recipeList);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllRecipes() {
        List<Recipe> recipeList = recipeService.getAllRecipes();
        return ResponseEntity.ok(recipeList);
    }

    @GetMapping("/marketplace")
    public ResponseEntity<?> getAllMarketplaceRecipesByUserId(
            @RequestHeader("Authorization") String header
    ) {
        List<Recipe> recipeList = recipeService.getAllMarketplaceRecipesByUserId(header);
        return ResponseEntity.ok(recipeList);
    }

    @PostMapping("")
    public ResponseEntity<?> createRecipe(
            @RequestBody Recipe recipe,
            @RequestHeader("Authorization") String header
    ) {
        Recipe newRecipe = recipeService.createRecipe(header, recipe);
        return ResponseEntity.ok(newRecipe);
    }

    @PostMapping("/itemset-to-recipe")
    public ResponseEntity<?> convertItemSetToRecipe(
            @RequestBody ItemSet itemSet,
            @RequestHeader("Authorization") String header
    ) {
        try {
            Recipe newRecipe = recipeService.convertItemSetToRecipe(header, itemSet);
            return ResponseEntity.ok(newRecipe);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PutMapping("/{recipeId}/visibility")
    public ResponseEntity<?> changeVisibility(
            @PathVariable String recipeId,
            @RequestBody Visibility visibility,
            @RequestHeader("Authorization") String header
    ) {
        //TODO create global exception handler
        try {
            Recipe updatedRecipe = recipeService.changeVisibility(header, recipeId, visibility);
            return ResponseEntity.ok(updatedRecipe);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateRecipe(
            @RequestPart Recipe recipe,
            @RequestPart(value="recipeFiles", required = false) List<MultipartFile> recipeFiles,
            @RequestHeader("Authorization") String header
    ) {
        try {
            Recipe updatedRecipe = recipeService.updateRecipe(header, recipe, recipeFiles);
            return ResponseEntity.ok(updatedRecipe);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PutMapping("/{recipeId}/save")
    public ResponseEntity<?> addRecipeToUser(
            @PathVariable String recipeId,
            @RequestParam(required = false) String username,
            @RequestHeader("Authorization") String header) {
        try {
            List<Recipe> updatedRecipeList = recipeService.addRecipeToUser(header, recipeId, username);
            return ResponseEntity.ok(updatedRecipeList);
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/{recipeId}/files/{recipeFileId}")
    public ResponseEntity<?> getRecipeFiles(@PathVariable String recipeId, @PathVariable String recipeFileId) {
        try {
            FileResourceDTO fileResourceDTO = recipeService.getRecipeFile(recipeId, recipeFileId);

            byte[] fileBytes = fileResourceDTO.getInputStreamResource().getInputStream().readAllBytes();
            ByteArrayResource byteArrayResource = new ByteArrayResource(fileBytes);

            return ResponseEntity.ok()
                    .contentLength(fileBytes.length)
                    .contentType(MediaType.parseMediaType(fileResourceDTO.getContentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + fileResourceDTO.getFilename() + "\"")
                    .body(byteArrayResource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/categories-popularity")
    public ResponseEntity<?> getRecipeCategoriesByPopularity(
            @RequestHeader("Authorization") String header
    ) {
        List<String> categoriesByPopularity = recipeService.getRecipeCategoriesByPopularity();
        return ResponseEntity.ok(categoriesByPopularity);
    }

    @DeleteMapping("/{recipeId}/remove")
    public ResponseEntity<?> removeRecipeFromUser(
            @PathVariable String recipeId,
            @RequestParam(required = false) String userId,
            @RequestHeader("Authorization") String header) {

        try {
            recipeService.removeRecipeFromUser(header, recipeId, userId);
            return ResponseEntity.ok(Map.of("message", "Recipe successfully removed!"));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @DeleteMapping("/{recipeId}")
    public ResponseEntity<?> deleteRecipe(
            @PathVariable String recipeId,
            @RequestHeader("Authorization") String header
    ) {
        try {
            recipeService.deleteRecipe(header, recipeId);
            return ResponseEntity.ok(Map.of("message", "Recipe successfully deleted!"));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }


}

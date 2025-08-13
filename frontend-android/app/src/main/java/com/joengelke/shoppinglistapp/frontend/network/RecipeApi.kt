package com.joengelke.shoppinglistapp.frontend.network

import com.joengelke.shoppinglistapp.frontend.models.DeleteResponse
import com.joengelke.shoppinglistapp.frontend.models.ItemSet
import com.joengelke.shoppinglistapp.frontend.models.Recipe
import com.joengelke.shoppinglistapp.frontend.models.Visibility
import retrofit2.Response
import retrofit2.http.*

interface RecipeApi {

    @GET("recipe")
    suspend fun getRecipesByUserId(
        @Header("Authorization") token: String
    ): Response<List<Recipe>>

    @GET("recipe/all")
    suspend fun getAllRecipes(
        @Header("Authorization") token: String
    ): Response<List<Recipe>>

    @GET("recipe/marketplace")
    suspend fun getAllMarketplaceRecipesByUserId(
        @Header("Authorization") token: String
    ): Response<List<Recipe>>

    @POST("recipe")
    suspend fun createRecipe(
        @Header("Authorization") token: String,
        @Body recipe: Recipe
    ): Response<Recipe>

    @POST("recipe/itemset-to-recipe")
    suspend fun convertItemSetToRecipe(
        @Header("Authorization") token: String,
        @Body itemSet: ItemSet
    ): Response<Recipe>

    @PUT("recipe/{recipeId}/visibility")
    suspend fun changeVisibility(
        @Header("Authorization") token: String,
        @Path("recipeId") recipeId: String,
        @Body visibility: Visibility,
    ): Response<Recipe>

    @PUT("recipe/update")
    suspend fun updateRecipe(
        @Header("Authorization") token: String,
        @Body recipe: Recipe
    ): Response<Recipe>

    @PUT("recipe/{recipeId}/save")
    suspend fun addRecipeToUser(
        @Header("Authorization") token: String,
        @Path("recipeId") recipeId: String,
        @Query("username") username: String?
    ): Response<List<Recipe>>

    @DELETE("recipe/{recipeId}/remove")
    suspend fun removeRecipeFromUser(
        @Header("Authorization") token: String,
        @Path("recipeId") recipeId: String,
        @Query("userId") userId: String?
    ): Response<DeleteResponse>

    @DELETE("recipe/{recipeId}")
    suspend fun deleteRecipe(
        @Header("Authorization") token: String,
        @Path("recipeId") recipeId: String
    ): Response<DeleteResponse>
}
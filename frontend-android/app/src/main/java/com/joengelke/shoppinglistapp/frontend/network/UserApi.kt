package com.joengelke.shoppinglistapp.frontend.network

import com.joengelke.shoppinglistapp.frontend.models.*
import retrofit2.Response
import retrofit2.http.*

interface UserApi {

    @GET("user")
    suspend fun getAllUsers(
        @Header("Authorization") token: String
    ): Response<List<User>>

    @GET("shoppinglist/{shoppingListId}/user")
    suspend fun getShoppingListUser(
        @Header("Authorization") token: String,
        @Path("shoppingListId") shoppingListId: String
    ): Response<List<User>>

    @GET("user/recipe-ids")
    suspend fun getCurrentUserRecipeIds(
        @Header("Authorization") token: String
    ): Response<List<String>>

    @POST("shoppinglist/{shoppingListId}/user")
    suspend fun addUserToShoppingList(
        @Header("Authorization") token: String,
        @Path("shoppingListId") shoppingListId: String,
        @Body request: AddUserRequest
    ): Response<User>

    @DELETE("shoppinglist/{shoppingListId}/user/{userId}")
    suspend fun removeUserFromShoppingList(
        @Header("Authorization") token: String,
        @Path("shoppingListId") shoppingListId: String,
        @Path("userId") userId: String
    ): Response<DeleteResponse>

    @PUT("user/username")
    suspend fun changeUsername(
        @Header("Authorization") token: String,
        @Body request: ChangeUsernameRequest
    ): Response<User>

    @PUT("user/password")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Body request: ChangePasswordRequest
    ): Response<User>

    @PUT("user/{userId}/add-role")
    suspend fun addRoleToUser(
        @Header("Authorization") token: String,
        @Path("userId") userId: String,
        @Query("role") role: String
    ): Response<User>

    @PUT("user/{userId}/remove-role")
    suspend fun removeRoleFromUser(
        @Header("Authorization") token: String,
        @Path("userId") userId: String,
        @Query("role") role: String
    ): Response<User>

    @DELETE("shoppinglist/user/{userId}")
    suspend fun deleteUser(
        @Header("Authorization") token: String,
        @Path("userId") userId: String
    ): Response<DeleteResponse>
}
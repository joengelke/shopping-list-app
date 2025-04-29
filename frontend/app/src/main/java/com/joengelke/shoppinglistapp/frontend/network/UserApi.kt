package com.joengelke.shoppinglistapp.frontend.network

import com.joengelke.shoppinglistapp.frontend.models.AddUserRequest
import com.joengelke.shoppinglistapp.frontend.models.DeleteResponse
import com.joengelke.shoppinglistapp.frontend.models.User
import retrofit2.Response
import retrofit2.http.*

interface UserApi {

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
    ): Response<User> //

    @GET("shoppinglist/{shoppingListId}/user")
    suspend fun getShoppingListUser(
        @Header("Authorization") token: String,
        @Path("shoppingListId") shoppingListId: String
    ): Response<List<User>>

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
}
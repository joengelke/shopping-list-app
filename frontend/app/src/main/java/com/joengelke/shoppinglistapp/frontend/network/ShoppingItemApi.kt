package com.joengelke.shoppinglistapp.frontend.network

import com.joengelke.shoppinglistapp.frontend.models.ShoppingItem
import com.joengelke.shoppinglistapp.frontend.models.ShoppingItemCreateRequest
import retrofit2.Response
import retrofit2.http.*

interface ShoppingItemApi {
    @GET("shoppinglist/{shoppingListId}/items")
    suspend fun getItemsByShoppingList(
        @Header("Authorization") token: String,
        @Path("shoppingListId") id: String
    ): Response<List<ShoppingItem>>

    // add item to shopping list
    @PUT("shoppinglist/{shoppingListId}/item")
    suspend fun addItemToShoppingList(
        @Header("Authorization") token: String,
        @Path("shoppingListId") shoppingListId: String,
        @Body shoppingItem: ShoppingItemCreateRequest
    ): Response<ShoppingItem>

    // update checked status
    @PATCH("shoppingitem/{itemId}/checked")
    suspend fun updateCheckedStatus(
        @Header("Authorization") token: String,
        @Path("itemId") itemId: String,
        @Query("checked") checked: Boolean
    ): Response<ShoppingItem>
}
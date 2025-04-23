package com.joengelke.shoppinglistapp.frontend.network

import com.joengelke.shoppinglistapp.frontend.models.DeleteResponse
import com.joengelke.shoppinglistapp.frontend.models.ItemSetItem
import com.joengelke.shoppinglistapp.frontend.models.ShoppingItem
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
    suspend fun addOneItemToShoppingList(
        @Header("Authorization") token: String,
        @Path("shoppingListId") shoppingListId: String,
        @Body shoppingItem: ShoppingItem
    ): Response<ShoppingItem>

    @PATCH("shoppingitem/{itemId}")
    suspend fun removeOneItemFromShoppingList(
        @Header("Authorization") token: String,
        @Path("itemId") itemId: String
    ): Response<ShoppingItem>

    // update checked status
    @PATCH("shoppingitem/{itemId}/checked")
    suspend fun updateCheckedStatus(
        @Header("Authorization") token: String,
        @Path("itemId") itemId: String,
        @Query("checked") checked: Boolean
    ): Response<ShoppingItem>

    @PUT("shoppingitem")
    suspend fun updateItem(
        @Header("Authorization") token: String,
        @Body shoppingItem: ShoppingItem
    ): Response<ShoppingItem>

    @DELETE("shoppinglist/{shoppingListId}/item/{itemId}")
    suspend fun deleteItem(
        @Header("Authorization") token: String,
        @Path("shoppingListId") shoppingListId: String,
        @Path("itemId") itemId: String
    ): Response<DeleteResponse>

    @PUT("shoppingitem/addItemSetItem")
    suspend fun addItemSetItemToShoppingList(
        @Header("Authorization") token: String,
        @Body itemSetItem: ItemSetItem
    ): Response<ShoppingItem>

    @PUT("shoppingitem/addAllItemSetItems/{itemSetId}")
    suspend fun addAllItemSetItemsToShoppingList(
        @Header("Authorization") token: String,
        @Path("itemSetId") itemSetId: String
    ): Response<List<ShoppingItem>>

    @PUT("shoppingitem/removeItemSetItem")
    suspend fun removeItemSetItemFromShoppingList(
        @Header("Authorization") token: String,
        @Body itemSetItem: ItemSetItem
    ): Response<ShoppingItem>

    @PUT("shoppingitem/removeAllItemSetItems/{itemSetId}")
    suspend fun removeAllItemSetItemsFromShoppingList(
        @Header("Authorization") token: String,
        @Path("itemSetId") itemSetId: String
    ): Response<List<ShoppingItem>>
}
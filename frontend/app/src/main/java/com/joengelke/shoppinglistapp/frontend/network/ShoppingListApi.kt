package com.joengelke.shoppinglistapp.frontend.network

import com.joengelke.shoppinglistapp.frontend.models.DeleteResponse
import com.joengelke.shoppinglistapp.frontend.models.ShoppingList
import com.joengelke.shoppinglistapp.frontend.models.ShoppingListCreateRequest
import retrofit2.Response
import retrofit2.http.*

interface ShoppingListApi {
    @GET("shoppinglist")
    suspend fun getShoppingLists(@Header("Authorization") token: String): Response<List<ShoppingList>>

    @GET("shoppinglist/uncheckedItemsAmount")
    suspend fun getUncheckedItemsAmountList(@Header("Authorization") token: String): Response<Map<String, Int>>

    @POST("shoppinglist")
    suspend fun createShoppingList(
        @Header("Authorization") token: String,
        @Body request: ShoppingListCreateRequest
    ): Response<ShoppingList>

    @PUT("shoppinglist")
    suspend fun updateShoppingList(
        @Header("Authorization") token: String,
        @Body shoppingList: ShoppingList
    ): Response<ShoppingList>

    @DELETE("shoppinglist/{shoppingListId}")
    suspend fun deleteShoppingList(
        @Header("Authorization") token: String,
        @Path("shoppingListId") shoppingListId: String,
    ): Response<DeleteResponse>

}
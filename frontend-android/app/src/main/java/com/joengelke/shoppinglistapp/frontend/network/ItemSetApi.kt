package com.joengelke.shoppinglistapp.frontend.network

import com.joengelke.shoppinglistapp.frontend.models.DeleteResponse
import com.joengelke.shoppinglistapp.frontend.models.ItemSet
import retrofit2.Response
import retrofit2.http.*

interface ItemSetApi {

    @GET("shoppinglist/{shoppingListId}/itemsets")
    suspend fun getItemSetsByShoppingList(
        @Header("Authorization") token: String,
        @Path("shoppingListId") shoppingListId: String
    ): Response<List<ItemSet>>

    @POST("shoppinglist/{shoppingListId}/itemset")
    suspend fun createItemSet(
        @Header("Authorization") token: String,
        @Path("shoppingListId") shoppingListId: String,
        @Body itemSet: ItemSet
    ) : Response<ItemSet>

    @PUT("shoppinglist/{shoppingListId}/itemset")
    suspend fun updateItemSet(
        @Header("Authorization") token: String,
        @Path("shoppingListId") shoppingListId: String,
        @Body itemSet: ItemSet
    ): Response<ItemSet>

    @DELETE("shoppinglist/{shoppingListId}/itemset/{itemSetId}")
    suspend fun deleteItemSet(
        @Header("Authorization") token: String,
        @Path("shoppingListId") shoppingListId: String,
        @Path("itemSetId") itemSetId: String
    ): Response<DeleteResponse>
}
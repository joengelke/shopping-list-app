package com.joengelke.shoppinglistapp.frontend.network

import com.joengelke.shoppinglistapp.frontend.models.ShoppingList
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

data class ShoppingListCreateRequest(val name: String)
data class ShoppingListResponse(val name: String, val createdAt: String)

interface ShoppingListApi {
    @GET("shoppinglist")
    suspend fun getShoppingLists(@Header("Authorization") token: String): Response<List<ShoppingList>>

    @POST("shoppinglist")
    suspend fun createShoppingList(
        @Header("Authorization") token: String,
        @Body request: ShoppingListCreateRequest
    ): Response<ShoppingListResponse>
}
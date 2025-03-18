package com.joengelke.shoppinglistapp.frontend.network

import com.joengelke.shoppinglistapp.frontend.models.ShoppingList
import com.joengelke.shoppinglistapp.frontend.models.ShoppingListCreateRequest
import com.joengelke.shoppinglistapp.frontend.models.ShoppingListResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ShoppingListApi {
    @GET("shoppinglist")
    suspend fun getShoppingLists(@Header("Authorization") token: String): Response<List<ShoppingList>>

    @POST("shoppinglist")
    suspend fun createShoppingList(
        @Header("Authorization") token: String,
        @Body request: ShoppingListCreateRequest
    ): Response<ShoppingListResponse>
}
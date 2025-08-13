package com.joengelke.shoppinglistapp.frontend.network

import com.joengelke.shoppinglistapp.frontend.models.DeleteResponse
import com.joengelke.shoppinglistapp.frontend.models.ItemSet
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ItemSetApi {

    @GET("shoppinglist/{shoppingListId}/itemsets")
    suspend fun getItemSetsByShoppingList(
        @Header("Authorization") token: String,
        @Path("shoppingListId") shoppingListId: String
    ): Response<List<ItemSet>>

    @Multipart
    @POST("shoppinglist/{shoppingListId}/itemset")
    suspend fun createItemSet(
        @Header("Authorization") token: String,
        @Path("shoppingListId") shoppingListId: String,
        @Part("itemSet") itemSet: RequestBody,
        @Part receiptFile: MultipartBody.Part? = null
    ) : Response<ItemSet>

    @Multipart
    @PUT("shoppinglist/{shoppingListId}/itemset")
    suspend fun updateItemSet(
        @Header("Authorization") token: String,
        @Path("shoppingListId") shoppingListId: String,
        @Part("itemSet") itemSet: RequestBody,
        @Part receiptFile: MultipartBody.Part? = null
    ): Response<ItemSet>

    @Streaming
    @GET("itemset/{itemSetId}/receiptfile")
    suspend fun getReceiptFile(
        @Header("Authorization") token: String,
        @Path("itemSetId") itemSetId: String
    ): Response<ResponseBody>

    @DELETE("shoppinglist/{shoppingListId}/itemset/{itemSetId}")
    suspend fun deleteItemSet(
        @Header("Authorization") token: String,
        @Path("shoppingListId") shoppingListId: String,
        @Path("itemSetId") itemSetId: String
    ): Response<DeleteResponse>
}
package com.joengelke.shoppinglistapp.frontend.repository

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.joengelke.shoppinglistapp.frontend.models.DeleteResponse
import com.joengelke.shoppinglistapp.frontend.models.ShoppingItem
import com.joengelke.shoppinglistapp.frontend.models.ShoppingItemRequest
import com.joengelke.shoppinglistapp.frontend.network.ShoppingItemApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.EOFException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShoppingItemRepository @Inject constructor(
    private val authRepository: AuthRepository
) {


    private val gson: Gson = GsonBuilder()
        //.setDateFormat("dd.MM.yyyy HH:mm:ss") // German format: 22.03.2025 12:37:55
        .create()

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://192.168.1.38:8080/api/")
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    private val shoppingItemApi = retrofit.create(ShoppingItemApi::class.java)

    suspend fun getItemsByShoppingList(shoppingListId: String): Result<List<ShoppingItem>> {
        return try {
            val token =
                authRepository.getToken() ?: return Result.failure(Exception("No token found"))

            val response = shoppingItemApi.getItemsByShoppingList("Bearer $token", shoppingListId)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to fetch items"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    suspend fun addOneItemToShoppingList(
        shoppingListId: String,
        shoppingItemName: String
    ): Result<ShoppingItem> {
        return try {
            val token =
                authRepository.getToken() ?: return Result.failure(Exception("No token found"))


            val shoppingItemRequest = ShoppingItemRequest(shoppingItemName)
            val response =
                shoppingItemApi.addOneItemToShoppingList(
                    "Bearer $token",
                    shoppingListId,
                    shoppingItemRequest
                )
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Unexpected empty response"))
            } else {
                Result.failure(Exception("Failed to add item: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    suspend fun removeOneItemOfShoppingList(
        shoppingListId: String,
        shoppingItemId: String
    ): Result<ShoppingItem?> {
        try {
            val token =
                authRepository.getToken() ?: return Result.failure(Exception("No token found"))

            val response =
                shoppingItemApi.removeOneItemFromShoppingList(
                    "Bearer $token",
                    shoppingListId,
                    shoppingItemId
                )

            return if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.success(null)
            } else {
                Result.failure(Exception("Failed to remove one item"))
            }

        } catch (e: EOFException) {
            // catches if response is null (item deleted) and thus returns an EOFException
            return Result.success(null)
        } catch (e: Exception) {
            return Result.failure(Exception("Network error: ${e.message}"))
        }
    }


    suspend fun updateCheckedStatus(itemId: String, checked: Boolean): Result<ShoppingItem> {
        return try {
            val token =
                authRepository.getToken() ?: return Result.failure(Exception("No token found"))

            val response =
                shoppingItemApi.updateCheckedStatus("Bearer $token", itemId, checked)
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Unexpected empty response"))
            } else {
                Result.failure(Exception("Failed to update item: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    suspend fun updateItem(updatedItem: ShoppingItem) : Result<ShoppingItem> {
        return try {
            val token =
                authRepository.getToken() ?: return Result.failure(Exception("No token found"))

            val response =
                shoppingItemApi.updateItem("Bearer $token", updatedItem)
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Unexpected empty response"))
            } else {
                Result.failure(Exception("Failed to update item: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    suspend fun deleteItem(shoppingListId: String, itemId: String): Result<DeleteResponse> {
        return try {
            val token =
                authRepository.getToken() ?: return Result.failure(Exception("No token found"))

            val response =
                shoppingItemApi.deleteItem("Bearer $token", shoppingListId, itemId)
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Unexpected empty response"))
            } else {
                Result.failure(Exception("Failed to update item: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }
}
package com.joengelke.shoppinglistapp.frontend.repository

import android.content.Context
import com.joengelke.shoppinglistapp.frontend.models.DeleteResponse
import com.joengelke.shoppinglistapp.frontend.models.ShoppingItem
import com.joengelke.shoppinglistapp.frontend.network.NetworkClient
import com.joengelke.shoppinglistapp.frontend.network.ShoppingItemApi
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShoppingItemRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository
) {

    private val shoppingItemApi: ShoppingItemApi = NetworkClient.createRetrofit(context).create(
        ShoppingItemApi::class.java)

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
        shoppingItem: ShoppingItem
    ): Result<ShoppingItem> {
        return try {
            val token =
                authRepository.getToken() ?: return Result.failure(Exception("No token found"))


            //val shoppingItemRequest = ShoppingItemRequest(shoppingItemName)
            val response =
                shoppingItemApi.addOneItemToShoppingList(
                    "Bearer $token",
                    shoppingListId,
                    shoppingItem
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
        shoppingItemId: String
    ): Result<ShoppingItem> {
        try {
            val token =
                authRepository.getToken() ?: return Result.failure(Exception("No token found"))

            val response =
                shoppingItemApi.removeOneItemFromShoppingList(
                    "Bearer $token",
                    shoppingItemId
                )
            return if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Unexpected empty response"))
            } else {
                Result.failure(Exception("Failed to remove one item"))
            }
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

    suspend fun updateItem(updatedItem: ShoppingItem): Result<ShoppingItem> {
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
package com.joengelke.shoppinglistapp.frontend.repository

import android.content.Context
import com.joengelke.shoppinglistapp.frontend.models.DeleteResponse
import com.joengelke.shoppinglistapp.frontend.models.ShoppingList
import com.joengelke.shoppinglistapp.frontend.models.ShoppingListCreateRequest
import com.joengelke.shoppinglistapp.frontend.network.NetworkClient
import com.joengelke.shoppinglistapp.frontend.network.ShoppingListApi
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShoppingListRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository
) {

    private val shoppingListApi: ShoppingListApi = NetworkClient.createRetrofit(context).create(
        ShoppingListApi::class.java)

    suspend fun getShoppingLists(): Result<List<ShoppingList>> {
        return try {
            val token =
                authRepository.getToken() ?: return Result.failure(Exception("No token found"))

            val response = shoppingListApi.getShoppingLists("Bearer $token")
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to fetch shopping lists"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    suspend fun getUncheckedItemsAmountList(): Result<Map<String, Int>> {
        return try {
            val token =
                authRepository.getToken() ?: return Result.failure(Exception("No token found"))

            val response = shoppingListApi.getUncheckedItemsAmountList("Bearer $token")
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyMap())
            } else {
                Result.failure(Exception("Failed to fetch shopping lists"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    suspend fun createShoppingList(name: String): Result<ShoppingList> {
        return try {
            val token =
                authRepository.getToken() ?: return Result.failure(Exception("No token found"))

            val response =
                shoppingListApi.createShoppingList("Bearer $token", ShoppingListCreateRequest(name))
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Unexpected empty response"))
            } else {
                Result.failure(Exception("Failed to create shopping list: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    suspend fun updateShoppingList(shoppingList: ShoppingList): Result<ShoppingList> {
        return try {
            val token =
                authRepository.getToken() ?: return Result.failure(Exception("No token found"))

            val response =
                shoppingListApi.updateShoppingList("Bearer $token", shoppingList)
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

    suspend fun deleteShoppingList(shoppingListId: String): Result<DeleteResponse> {
        return try {
            val token =
                authRepository.getToken() ?: return Result.failure(Exception("No token found"))

            val response =
                shoppingListApi.deleteShoppingList("Bearer $token", shoppingListId)

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
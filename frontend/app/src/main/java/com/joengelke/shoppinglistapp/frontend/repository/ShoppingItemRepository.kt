package com.joengelke.shoppinglistapp.frontend.repository

import com.joengelke.shoppinglistapp.frontend.models.ShoppingItem
import com.joengelke.shoppinglistapp.frontend.models.ShoppingItemCreateRequest
import com.joengelke.shoppinglistapp.frontend.network.ShoppingItemApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShoppingItemRepository @Inject constructor(
    private val authRepository: AuthRepository
) {
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://192.168.1.38:8080/api/")
        .addConverterFactory(GsonConverterFactory.create())
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

    suspend fun addItemToShoppingList(
        shoppingListId: String,
        shoppingItem: ShoppingItemCreateRequest
    ): Result<ShoppingItem> {
        return try {
            val token =
                authRepository.getToken() ?: return Result.failure(Exception("No token found"))

            val response =
                shoppingItemApi.addItemToShoppingList("Bearer $token", shoppingListId, shoppingItem)
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
}
package com.joengelke.shoppinglistapp.frontend.repository

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.joengelke.shoppinglistapp.frontend.models.ShoppingList
import com.joengelke.shoppinglistapp.frontend.models.ShoppingListCreateRequest
import com.joengelke.shoppinglistapp.frontend.models.ShoppingListResponse
import com.joengelke.shoppinglistapp.frontend.network.ShoppingListApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShoppingListRepository @Inject constructor(
    private val authRepository: AuthRepository
) {
    private val gson: Gson = GsonBuilder()
        .setDateFormat("dd.MM.yyyy HH:mm:ss") // German format: 22.03.2025 12:37:55
        .create()

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://192.168.1.38:8080/api/")
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    private val shoppingListApi = retrofit.create(ShoppingListApi::class.java)

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

    suspend fun createShoppingList(name: String): Result<ShoppingListResponse> {
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
}
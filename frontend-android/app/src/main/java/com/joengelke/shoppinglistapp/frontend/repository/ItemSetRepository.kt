package com.joengelke.shoppinglistapp.frontend.repository

import com.joengelke.shoppinglistapp.frontend.common.exception.AppException
import com.joengelke.shoppinglistapp.frontend.models.DeleteResponse
import com.joengelke.shoppinglistapp.frontend.models.ItemSet
import com.joengelke.shoppinglistapp.frontend.network.RetrofitProvider
import com.joengelke.shoppinglistapp.frontend.network.SessionManager
import com.joengelke.shoppinglistapp.frontend.network.TokenManager
import com.joengelke.shoppinglistapp.frontend.utils.JsonHelper.json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ItemSetRepository @Inject constructor(
    private val sessionManager: SessionManager,
    private val tokenManager: TokenManager,
    private val retrofitProvider: RetrofitProvider
) {

    suspend fun getItemSetsByShoppingList(shoppingListId: String): Result<List<ItemSet>> {
        return try {
            val token =
                tokenManager.getToken() ?: return Result.failure(Exception("No token found"))

            val response = retrofitProvider.getItemSetApi().getItemSetsByShoppingList("Bearer $token", shoppingListId)
            when {
                response.isSuccessful ->  Result.success(response.body() ?: emptyList())
                response.code() == 401 -> {
                    sessionManager.logout("Unauthorized: try to login again ")
                    Result.failure(Exception("Unauthorized"))
                }

                else -> Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            sessionManager.disconnected("No connection to the Server")
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    suspend fun createItemSet(shoppingListId: String, itemSet: ItemSet): Result<ItemSet> {
        return try {
            val token =
                tokenManager.getToken() ?: return Result.failure(Exception("No token found"))

            // Serialize ItemSet to JSON RequestBody
            val jsonString = json.encodeToString(ItemSet.serializer(), itemSet)
            val itemSetBody = jsonString.toRequestBody("application/json".toMediaType())

            val response = retrofitProvider.getItemSetApi().createItemSet("Bearer $token", shoppingListId, itemSetBody)
            when {
                response.isSuccessful -> response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Unexpected empty response"))

                response.code() == 409 -> {
                    Result.failure(AppException.ItemSetNameDuplicationException())
                }

                response.code() == 401 -> {
                    sessionManager.logout("Unauthorized: try to login again ")
                    Result.failure(Exception("Unauthorized"))
                }

                else -> Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            sessionManager.disconnected("No connection to the Server")
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    suspend fun updateItemSet(shoppingListId: String, itemSet: ItemSet): Result<ItemSet> {
        return try {
            val token =
                tokenManager.getToken() ?: return Result.failure(Exception("No token found"))

            // filter out itemsetitems with empty name
            val cleanedItemSet = itemSet.copy(
                itemList = itemSet.itemList.filter { it.name.isNotBlank() }
            )

            // Serialize ItemSet to JSON RequestBody
            val jsonString = json.encodeToString(ItemSet.serializer(), cleanedItemSet)
            val itemSetBody = jsonString.toRequestBody("application/json".toMediaType())

            val response = retrofitProvider.getItemSetApi().updateItemSet("Bearer $token", shoppingListId, itemSetBody)
            when {
                response.isSuccessful -> response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Unexpected empty response"))
                response.code() == 401 -> {
                    sessionManager.logout("Unauthorized: try to login again ")
                    Result.failure(Exception("Unauthorized"))
                }

                else -> Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            sessionManager.disconnected("No connection to the Server")
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    suspend fun deleteItemSet(shoppingListId: String, itemSetId: String): Result<DeleteResponse> {
        return try {
            val token =
                tokenManager.getToken() ?: return Result.failure(Exception("No token found"))

            val response =
                retrofitProvider.getItemSetApi()
                    .deleteItemSet("Bearer $token", shoppingListId, itemSetId)
            when {
                response.isSuccessful -> response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Unexpected empty response"))
                response.code() == 401 -> {
                    sessionManager.logout("Unauthorized: try to login again ")
                    Result.failure(Exception("Unauthorized"))
                }

                else -> Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            sessionManager.disconnected("No connection to the Server")
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

}
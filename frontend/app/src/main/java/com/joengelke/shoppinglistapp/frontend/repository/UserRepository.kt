package com.joengelke.shoppinglistapp.frontend.repository

import android.content.Context
import com.joengelke.shoppinglistapp.frontend.models.AddUserRequest
import com.joengelke.shoppinglistapp.frontend.models.DeleteResponse
import com.joengelke.shoppinglistapp.frontend.models.User
import com.joengelke.shoppinglistapp.frontend.network.NetworkModule
import com.joengelke.shoppinglistapp.frontend.network.SessionManager
import com.joengelke.shoppinglistapp.frontend.network.TokenManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionManager: SessionManager,
    private val tokenManager: TokenManager
){
    suspend fun addRoleToUser(
        userId: String,
        role: String
    ) : Result<User> {
        return try {
            val token =
                tokenManager.getToken() ?: return Result.failure(Exception("No token found"))

            val response =
                NetworkModule.getUserApi(context)
                    .addRoleToUser(
                        "Bearer $token",
                        userId,
                        role
                    )
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

    suspend fun removeRoleFromUser(
        userId: String,
        role: String
    ) : Result<User> {
        return try {
            val token =
                tokenManager.getToken() ?: return Result.failure(Exception("No token found"))

            val response =
                NetworkModule.getUserApi(context)
                    .removeRoleFromUser(
                        "Bearer $token",
                        userId,
                        role
                    )
            when {
                response.isSuccessful -> response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Unexpected empty response"))

                response.code() == 401 -> {
                    sessionManager.logout("Unauthorized: try to login again ")
                    Result.failure(Exception("Unauthorized"))
                }

                // user cant remove own ADMIN role
                response.code() == 403 -> {
                    Result.failure(Exception("TODO"))
                }

                else -> Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            sessionManager.disconnected("No connection to the Server")
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    suspend fun getShoppingListUser(
        shoppingListId: String
    ) : Result<List<User>> {
        return try {
            val token =
                tokenManager.getToken() ?: return Result.failure(Exception("No token found"))

            val response =
                NetworkModule.getUserApi(context)
                    .getShoppingListUser(
                        "Bearer $token",
                        shoppingListId
                    )
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

    suspend fun addUserToShoppingList(
        shoppingListId: String,
        username: String
    ): Result<User> {
        return try {
            val token =
                tokenManager.getToken() ?: return Result.failure(Exception("No token found"))

            val response =
                NetworkModule.getUserApi(context)
                    .addUserToShoppingList(
                        "Bearer $token",
                        shoppingListId,
                        AddUserRequest(username)
                    )
            when {
                response.isSuccessful -> response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Unexpected empty response"))

                response.code() == 401 -> {
                    sessionManager.logout("Unauthorized: try to login again ")
                    Result.failure(Exception("Unauthorized"))
                }

                response.code() == 404 -> {
                    // handle UsernameNotFoundException from Server
                    Result.failure(Exception("Username not found"))
                }

                else -> Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            sessionManager.disconnected("No connection to the Server")
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    suspend fun removeUserFromShoppingList(shoppingListId: String, userId: String): Result<DeleteResponse> {
        return try {
            val token =
                tokenManager.getToken() ?: return Result.failure(Exception("No token found"))

            val response =
                NetworkModule.getUserApi(context)
                    .removeUserFromShoppingList(
                        "Bearer $token",
                        shoppingListId,
                        userId
                    )
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
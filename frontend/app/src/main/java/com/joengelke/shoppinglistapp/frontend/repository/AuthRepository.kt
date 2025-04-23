package com.joengelke.shoppinglistapp.frontend.repository

import android.content.Context
import com.joengelke.shoppinglistapp.frontend.network.AuthRequest
import com.joengelke.shoppinglistapp.frontend.network.NetworkModule
import com.joengelke.shoppinglistapp.frontend.network.SessionManager
import com.joengelke.shoppinglistapp.frontend.network.TokenManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionManager: SessionManager,
    private val tokenManager: TokenManager
) {
    // perform login
    suspend fun login(username: String, password: String): Result<String?> {
        return try {
            val response = NetworkModule.getAuthApi(context).login(AuthRequest(username, password))
            when {
                response.isSuccessful -> {
                    val bearerToken = response.body()?.token
                    val token = bearerToken?.removePrefix("Bearer ")
                    token?.let { tokenManager.saveToken(it) }
                    Result.success(token ?: return Result.failure(Exception("Token missing in response")))
                }

                else -> Result.failure(Exception("Login failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            sessionManager.disconnected("No connection to the Server")
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    // perform register
    suspend fun register(username: String, password: String): Result<String> {
        return try {
            val response =
                NetworkModule.getAuthApi(context).register(AuthRequest(username, password))
            when {
                response.isSuccessful -> {
                    val message = response.body()?.username ?: "Registration successful"
                    Result.success("User $message registered")
                }

                else -> Result.failure(Exception("Username already taken"))
            }
        } catch (e: Exception) {
            sessionManager.disconnected("No connection to the Server")
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }
}
package com.joengelke.shoppinglistapp.frontend.repository

import android.content.Context
import android.content.Intent
import com.joengelke.shoppinglistapp.frontend.R
import com.joengelke.shoppinglistapp.frontend.datastore.LoginDataStore
import com.joengelke.shoppinglistapp.frontend.network.AuthRequest
import com.joengelke.shoppinglistapp.frontend.network.RetrofitProvider
import com.joengelke.shoppinglistapp.frontend.network.SessionManager
import com.joengelke.shoppinglistapp.frontend.network.TokenManager
import com.joengelke.shoppinglistapp.frontend.utils.AppActions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionManager: SessionManager,
    private val tokenManager: TokenManager,
    private val retrofitProvider: RetrofitProvider
) {
    val credentialsFlow = LoginDataStore.credentialsFlow(context)
    val saveCredentialsFlow = LoginDataStore.saveCredentialsFlow(context)

    private fun sendLoginStatusChangedBroadcast() {
        val intent = Intent(AppActions.ACTION_LOGIN_STATUS_CHANGED)
        context.sendBroadcast(intent)
    }

    // perform login
    suspend fun login(username: String, password: String): Result<String?> {
        //TODO change all Exceptions
        return try {
            val response = retrofitProvider.getAuthApi().login(AuthRequest(username, password))
            when {
                response.isSuccessful -> {
                    val bearerToken = response.body()?.token
                    val token = bearerToken?.removePrefix("Bearer ")
                    token?.let { tokenManager.saveToken(it) }

                    if(saveCredentialsFlow.first()) {
                        LoginDataStore.setCredentials(context, username, password)
                    } else {
                        LoginDataStore.clearCredentials(context)
                    }

                    LoginDataStore.setLoggedIn(context, true)
                    sendLoginStatusChangedBroadcast()
                    Result.success(
                        token ?: return Result.failure(Exception("Token missing in response"))
                    )
                }

                response.code() == 403 -> {
                    Result.failure(Exception(context.getString(R.string.wrong_username_or_password)))
                }

                else -> Result.failure(Exception("Login failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(context.getString(R.string.network_error_maybe_try_another_server_bottom_left)))
        }
    }

    // perform register
    suspend fun register(username: String, password: String): Result<String> {
        return try {
            val response =
                retrofitProvider.getAuthApi().register(AuthRequest(username, password))
            when {
                response.isSuccessful -> {
                    val message = response.body()?.username ?: "Registration successful"
                    Result.success("User $message registered")
                }

                response.code() == 403 -> {
                    Result.failure(Exception("Password requirements missing"))
                }

                response.code() == 409 -> {
                    Result.failure(Exception("Username already taken"))
                }

                else -> Result.failure(Exception("Unknown error: code ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(context.getString(R.string.network_error_maybe_try_another_server_bottom_left)))
        }
    }

    suspend fun setSaveCredentials(enabled: Boolean) {
        LoginDataStore.setSaveCredentials(context, enabled)
    }

    suspend fun logout(): Result<Unit> {
        return try {
            sessionManager.logout("Logged out")
            LoginDataStore.setLoggedIn(context, false)
            sendLoginStatusChangedBroadcast()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
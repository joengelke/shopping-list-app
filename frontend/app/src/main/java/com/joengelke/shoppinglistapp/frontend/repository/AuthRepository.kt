package com.joengelke.shoppinglistapp.frontend.repository

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.auth0.android.jwt.JWT
import com.joengelke.shoppinglistapp.frontend.network.AuthApi
import com.joengelke.shoppinglistapp.frontend.network.AuthRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore by preferencesDataStore(name = "preferences") // not sure if right place

@Singleton
class AuthRepository @Inject constructor(@ApplicationContext private val context: Context) {

    private val tokenKey = stringPreferencesKey("jwt_token")

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://192.168.1.38:8080/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val authApi = retrofit.create(AuthApi::class.java)

    // perform login
    suspend fun login(username: String, password: String): String? {
        Log.d("AuthApi", "Sending login request: $username, $password")
        val response = authApi.login(AuthRequest(username, password))
        return if (response.isSuccessful) {
            val bearerToken = response.body()?.token
            val token = bearerToken?.removePrefix("Bearer ")
            token?.let { saveToken(it) }
            token
        } else {
            null
        }
    }

    // perform register
    suspend fun register(username: String, password: String): Result<String> {
        return try {
            val response = authApi.register(AuthRequest(username, password))
            if (response.isSuccessful) {
                val message = response.body()?.username ?: "Registration successful"
                Result.success("User $message registered")
            } else {
                Result.failure(Exception("Username already taken"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    // save token in dataStore
    private suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[tokenKey] = token
        }
    }

    // Retrieve token
    suspend fun getToken(): String? {
        return context.dataStore.data.firstOrNull()?.get(tokenKey)
    }

    suspend fun validateToken(token: String): Boolean {
        return try {
            val jwt = JWT(token)
            val expirationTime = jwt.expiresAt
            expirationTime?.after(Date()) == true
        } catch (e: Exception) {
            false
        }
    }

    // logout and clear token
    suspend fun logout() {
        context.dataStore.edit { preferences ->
            preferences.remove(tokenKey)
        }
    }
}
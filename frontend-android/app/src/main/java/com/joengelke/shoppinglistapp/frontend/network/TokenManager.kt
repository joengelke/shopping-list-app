package com.joengelke.shoppinglistapp.frontend.network

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.auth0.android.jwt.JWT
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import java.time.Instant
import javax.inject.Inject

val Context.dataStore by preferencesDataStore(name = "preferences") // not sure if right place

class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val tokenKey = stringPreferencesKey("jwt_token")

    // Save token to DataStore
    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[tokenKey] = token
        }
    }

    // Retrieve token from DataStore
    suspend fun getToken(): String? {
        return context.dataStore.data.firstOrNull()?.get(tokenKey)
    }

    fun getUserIdFromToken(token: String): String {
        val jwt = JWT(token)
        return jwt.getClaim("userId").asString() ?: throw IllegalArgumentException("User ID missing from token")
    }

    fun getUsernameFromToken(token: String): String? {
        return try {
            val jwt = JWT(token)
            jwt.subject // This is the "sub" claim, which contains the username
        } catch (e: Exception) {
            null
        }
    }

    fun getAuthoritiesFromToken(token: String): List<String> {
        return try {
            val jwt = JWT(token)
            jwt.getClaim("authorities").asString()?.split(",") ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Validate token expiration
    fun validateToken(token: String): Boolean {
        return try {
            val jwt = JWT(token)
            val expirationTime = jwt.expiresAt?.toInstant()
            expirationTime?.isAfter(Instant.now()) == true
        } catch (e: Exception) {
            false
        }
    }

    // Delete token
    suspend fun deleteToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(tokenKey)
        }
    }
}
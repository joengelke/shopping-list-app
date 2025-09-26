package com.joengelke.shoppinglistapp.frontend.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.glance.appwidget.updateAll
import com.joengelke.shoppinglistapp.frontend.widget.ShoppingListWidget
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


object LoginDataStore {
    private val Context.dataStore by preferencesDataStore(name = "login_store")
    private val USERNAME_KEY = stringPreferencesKey("encrypted_username")
    private val PASSWORD_KEY = stringPreferencesKey("encrypted_password")
    private val SAVE_CREDENTIALS_KEY = booleanPreferencesKey("save_credentials")
    private val IS_LOGGED_IN_KEY = booleanPreferencesKey("is_logged_in")
    private val BIOMETRIC_ENABLED_KEY = booleanPreferencesKey("biometric_enabled")


    val credentialsFlow: (Context) -> Flow<Pair<String, String>?> = { context ->
        context.dataStore.data
            .map { prefs ->
                val encryptedUsername = prefs[USERNAME_KEY]
                val encryptedPassword = prefs[PASSWORD_KEY]
                if(encryptedUsername != null && encryptedPassword != null) {
                    val username = SecureCryptoManager.decrypt(encryptedUsername)
                    val password = SecureCryptoManager.decrypt(encryptedPassword)
                    username to password
                } else {
                    null
                }
            }
    }

    val saveCredentialsFlow: (Context) -> Flow<Boolean> = { context ->
        context.dataStore.data.map { prefs -> prefs[SAVE_CREDENTIALS_KEY] ?: false }
    }

    val isLoggedInFlow: (Context) -> Flow<Boolean> = { context ->
        context.dataStore.data.map { prefs ->
            prefs[IS_LOGGED_IN_KEY] ?: false
        }
    }

    suspend fun setCredentials(context: Context, username: String, password: String) {
        context.dataStore.edit { prefs ->
            prefs[USERNAME_KEY] = SecureCryptoManager.encrypt(username)
            prefs[PASSWORD_KEY] = SecureCryptoManager.encrypt(password)
        }
    }

    suspend fun clearCredentials(context: Context) {
        context.dataStore.edit{ prefs ->
            prefs.remove(USERNAME_KEY)
            prefs.remove(PASSWORD_KEY)
        }
    }

    suspend fun setSaveCredentials(context: Context, enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SAVE_CREDENTIALS_KEY] = enabled
        }
    }

    suspend fun setLoggedIn(context: Context, loggedIn: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[IS_LOGGED_IN_KEY] = loggedIn
        }
        ShoppingListWidget().updateAll(context)
    }
}
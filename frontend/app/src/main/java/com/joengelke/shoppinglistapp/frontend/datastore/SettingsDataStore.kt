package com.joengelke.shoppinglistapp.frontend.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.joengelke.shoppinglistapp.frontend.ui.common.ShoppingItemsSortCategory
import com.joengelke.shoppinglistapp.frontend.ui.common.ShoppingItemsSortOptions
import com.joengelke.shoppinglistapp.frontend.ui.common.SortDirection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object SettingsDataStore {
    private val Context.dataStore by preferencesDataStore(name = "settings")

    private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
    private val LANGUAGE_KEY = stringPreferencesKey("language")
    private val SHOPPING_ITEMS_SORT_OPTION_KEY = stringPreferencesKey("shopping_items_sort_option")

    suspend fun setDarkMode(context: Context, isDarkMode: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = isDarkMode
        }
    }

    suspend fun setLanguage(context: Context, languageCode: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = languageCode
        }
    }

    suspend fun setShoppingItemsSortOption(context: Context, option: ShoppingItemsSortOptions) {
        val value = "${option.category.name}|${option.direction.name}"
        context.dataStore.edit { preferences ->
            preferences[SHOPPING_ITEMS_SORT_OPTION_KEY] = value
        }
    }

    val darkModeFlow: (Context) -> Flow<Boolean> = { context ->
        context.dataStore.data.map { prefs -> prefs[DARK_MODE_KEY] ?: false }
    }

    val languageFlow: (Context) -> Flow<String> = { context ->
        context.dataStore.data.map { prefs -> prefs[LANGUAGE_KEY] ?: "en" }
    }

    val shoppingItemsSortOptionFlow: (Context) -> Flow<ShoppingItemsSortOptions> = { context ->
        context.dataStore.data.map { prefs ->
            val raw = prefs[SHOPPING_ITEMS_SORT_OPTION_KEY]
            val parts = raw?.split("|")

            val category = parts?.getOrNull(0)?.let { runCatching { ShoppingItemsSortCategory.valueOf(it) }.getOrNull() }
            val direction = parts?.getOrNull(1)?.let { runCatching { SortDirection.valueOf(it) }.getOrNull() }

            if (category != null && direction != null) {
                ShoppingItemsSortOptions(category, direction)
            } else {
                // fallback
                ShoppingItemsSortOptions(ShoppingItemsSortCategory.ALPHABETICAL, SortDirection.ASCENDING)
            }
        }
    }
}

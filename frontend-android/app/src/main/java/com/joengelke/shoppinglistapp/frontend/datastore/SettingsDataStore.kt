package com.joengelke.shoppinglistapp.frontend.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.joengelke.shoppinglistapp.frontend.ui.common.RecipesSortCategory
import com.joengelke.shoppinglistapp.frontend.ui.common.ShoppingItemsSortCategory
import com.joengelke.shoppinglistapp.frontend.ui.common.SortDirection
import com.joengelke.shoppinglistapp.frontend.ui.common.SortOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object SettingsDataStore {
    private val Context.dataStore by preferencesDataStore(name = "settings")

    private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
    private val FONT_SCALE_KEY = floatPreferencesKey("font_scale")
    private val SHOPPING_ITEMS_SORT_OPTION_KEY = stringPreferencesKey("shopping_items_sort_option")
    private val RECIPES_SORT_OPTION_KEY = stringPreferencesKey("recipes_sort_option")
    private val SERVER_URL_KEY = stringPreferencesKey("server_url")

    suspend fun setDarkMode(context: Context, isDarkMode: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = isDarkMode
        }
    }

    suspend fun setFontScale(context: Context, scale: Float) {
        context.dataStore.edit { preferences ->
            preferences[FONT_SCALE_KEY] = scale
        }
    }

    suspend fun setShoppingItemsSortOption(context: Context, option: SortOptions<ShoppingItemsSortCategory>) {
        val value = "${option.category.name}|${option.direction.name}"
        context.dataStore.edit { preferences ->
            preferences[SHOPPING_ITEMS_SORT_OPTION_KEY] = value
        }
    }

    suspend fun setRecipesSortOption(context: Context, option: SortOptions<RecipesSortCategory>) {
        val value = "${option.category.name}|${option.direction.name}"
        context.dataStore.edit { preferences ->
            preferences[RECIPES_SORT_OPTION_KEY] = value
        }
    }

    suspend fun setServerUrl(context: Context, url: String) {
        context.dataStore.edit { preferences ->
            preferences[SERVER_URL_KEY] = url
        }
    }

    val darkModeFlow: (Context) -> Flow<Boolean> = { context ->
        context.dataStore.data.map { prefs -> prefs[DARK_MODE_KEY] ?: false }
    }

    val fontScaleFlow: (Context) -> Flow<Float> = { context ->
        context.dataStore.data.map { prefs -> prefs[FONT_SCALE_KEY] ?: 1.0f }
    }

    val shoppingItemsSortOptionFlow: (Context) -> Flow<SortOptions<ShoppingItemsSortCategory>> = { context ->
        context.dataStore.data.map { prefs ->
            val raw = prefs[SHOPPING_ITEMS_SORT_OPTION_KEY]
            val parts = raw?.split("|")

            val category = parts?.getOrNull(0)
                ?.let { runCatching { ShoppingItemsSortCategory.valueOf(it) }.getOrNull() }
            val direction =
                parts?.getOrNull(1)?.let { runCatching { SortDirection.valueOf(it) }.getOrNull() }

            if (category != null && direction != null) {
                SortOptions(category, direction)
            } else {
                // fallback
                SortOptions(
                    ShoppingItemsSortCategory.ALPHABETICAL,
                    SortDirection.ASCENDING
                )
            }
        }
    }

    val recipesSortOptionFlow: (Context) -> Flow<SortOptions<RecipesSortCategory>> = { context ->
        context.dataStore.data.map { prefs ->
            val raw = prefs[RECIPES_SORT_OPTION_KEY]
            val parts = raw?.split("|")

            val category = parts?.getOrNull(0)
                ?.let { runCatching { RecipesSortCategory.valueOf(it) }.getOrNull() }
            val direction =
                parts?.getOrNull(1)?.let { runCatching { SortDirection.valueOf(it) }.getOrNull() }

            if (category != null && direction != null) {
                SortOptions(category, direction)
            } else {
                // fallback
                SortOptions(
                    RecipesSortCategory.ALPHABETICAL,
                    SortDirection.ASCENDING
                )
            }
        }
    }

    val serverUrlFlow: (Context) -> Flow<String> = { context ->
        context.dataStore.data.map { prefs -> prefs[SERVER_URL_KEY] ?: "https://shopit-oracle.mooo.com:8443/api/"}
    }
}

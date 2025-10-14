package com.joengelke.shoppinglistapp.frontend.repository

import android.content.Context
import com.joengelke.shoppinglistapp.frontend.datastore.SettingsDataStore
import com.joengelke.shoppinglistapp.frontend.ui.common.RecipesSortCategory
import com.joengelke.shoppinglistapp.frontend.ui.common.ShoppingItemsSortCategory
import com.joengelke.shoppinglistapp.frontend.ui.common.SortOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    val darkModeFlow = SettingsDataStore.darkModeFlow(context)
    val fontScaleFlow = SettingsDataStore.fontScaleFlow(context)
    val shoppingItemsSortOptionFlow = SettingsDataStore.shoppingItemsSortOptionFlow(context)
    val recipesSortOptionFLow = SettingsDataStore.recipesSortOptionFlow(context)
    val serverUrlFlow = SettingsDataStore.serverUrlFlow(context)

    suspend fun setDarkMode(enabled: Boolean) {
        SettingsDataStore.setDarkMode(context, enabled)
    }

    suspend fun setFontScale(scale: Float) {
        SettingsDataStore.setFontScale(context, scale)
    }

    suspend fun setShoppingItemsSortOption(option: SortOptions<ShoppingItemsSortCategory>) {
        SettingsDataStore.setShoppingItemsSortOption(context, option)
    }

    suspend fun setRecipesSortOption(option: SortOptions<RecipesSortCategory>) {
        SettingsDataStore.setRecipesSortOption(context, option)
    }

    suspend fun setServerUrl(url: String) {
        SettingsDataStore.setServerUrl(context, url)
    }
}
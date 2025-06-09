package com.joengelke.shoppinglistapp.frontend.repository

import android.content.Context
import com.joengelke.shoppinglistapp.frontend.datastore.SettingsDataStore
import com.joengelke.shoppinglistapp.frontend.ui.common.ShoppingItemsSortOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    val darkModeFlow = SettingsDataStore.darkModeFlow(context)
    val languageFlow = SettingsDataStore.languageFlow(context)
    val fontScaleFlow = SettingsDataStore.fontScaleFlow(context)
    val shoppingItemsSortOptionFlow = SettingsDataStore.shoppingItemsSortOptionFlow(context)

    suspend fun setDarkMode(enabled: Boolean) {
        SettingsDataStore.setDarkMode(context, enabled)
    }

    suspend fun setLanguage(lang: String) {
        SettingsDataStore.setLanguage(context, lang)
    }

    suspend fun setFontScale(scale: Float) {
        SettingsDataStore.setFontScale(context, scale)
    }

    suspend fun setShoppingItemsSortOption(option: ShoppingItemsSortOptions) {
        SettingsDataStore.setShoppingItemsSortOption(context, option)
    }
}
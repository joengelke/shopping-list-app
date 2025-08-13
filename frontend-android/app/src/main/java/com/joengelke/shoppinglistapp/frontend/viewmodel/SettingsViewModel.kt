package com.joengelke.shoppinglistapp.frontend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joengelke.shoppinglistapp.frontend.network.RetrofitProvider
import com.joengelke.shoppinglistapp.frontend.repository.SettingsRepository
import com.joengelke.shoppinglistapp.frontend.ui.common.RecipesSortCategory
import com.joengelke.shoppinglistapp.frontend.ui.common.ShoppingItemsSortCategory
import com.joengelke.shoppinglistapp.frontend.ui.common.SortDirection
import com.joengelke.shoppinglistapp.frontend.ui.common.SortOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val retrofitProvider: RetrofitProvider
) : ViewModel() {

    private val _darkMode = MutableStateFlow(false)
    val darkMode: StateFlow<Boolean> = _darkMode.asStateFlow()

    private val _language = MutableStateFlow("en")
    val language: StateFlow<String> = _language.asStateFlow()

    private val _fontScale = MutableStateFlow(1.0f)
    val fontScale: StateFlow<Float> = _fontScale.asStateFlow()

    private val _shoppingItemsSortOption = MutableStateFlow(
        SortOptions(
            category = ShoppingItemsSortCategory.ALPHABETICAL,
            direction = SortDirection.ASCENDING
        )
    )
    val shoppingItemsSortOption: StateFlow<SortOptions<ShoppingItemsSortCategory>> =
        _shoppingItemsSortOption.asStateFlow()

    private val _recipesSortOption = MutableStateFlow(
        SortOptions(
            category = RecipesSortCategory.ALPHABETICAL,
            direction = SortDirection.ASCENDING
        )
    )
    val recipesSortOption: StateFlow<SortOptions<RecipesSortCategory>> =
        _recipesSortOption.asStateFlow()

    private val _serverUrl = MutableStateFlow("https://shopit.mooo.com:8443/api/")
    val serverUrl: StateFlow<String> = _serverUrl.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.darkModeFlow.collect { _darkMode.value = it }
        }
        viewModelScope.launch {
            settingsRepository.languageFlow.collect { _language.value = it }
        }
        viewModelScope.launch {
            settingsRepository.fontScaleFlow.collect { _fontScale.value = it }
        }
        viewModelScope.launch {
            settingsRepository.shoppingItemsSortOptionFlow.collect {
                _shoppingItemsSortOption.value = it
            }
        }
        viewModelScope.launch {
            settingsRepository.recipesSortOptionFLow.collect {
                _recipesSortOption.value = it
            }
        }
        viewModelScope.launch {
            settingsRepository.serverUrlFlow.collect { _serverUrl.value = it }
        }
    }

    fun toggleDarkMode() {
        viewModelScope.launch {
            val newValue = !_darkMode.value
            settingsRepository.setDarkMode(newValue)
        }
    }

    fun setLanguage(language: String) {
        viewModelScope.launch {
            settingsRepository.setLanguage(language)
        }
    }

    fun setFontScale(scale: Float) {
        viewModelScope.launch {
            settingsRepository.setFontScale(scale)
        }
    }

    fun setShoppingItemsSortOption(option: SortOptions<ShoppingItemsSortCategory>) {
        viewModelScope.launch {
            settingsRepository.setShoppingItemsSortOption(option)
        }
    }

    fun setRecipesSortOption(option: SortOptions<RecipesSortCategory>) {
        viewModelScope.launch {
            settingsRepository.setRecipesSortOption(option)
        }
    }

    fun setServerUrl(url: String) {
        viewModelScope.launch {
            settingsRepository.setServerUrl(url)
            retrofitProvider.initialize()
        }
    }
}
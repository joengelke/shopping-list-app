package com.joengelke.shoppinglistapp.frontend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joengelke.shoppinglistapp.frontend.models.ShoppingList
import com.joengelke.shoppinglistapp.frontend.repository.ShoppingListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShoppingListViewModel @Inject constructor(private val shoppingListRepository: ShoppingListRepository) :
    ViewModel() {

    private val _shoppingLists = MutableStateFlow<List<ShoppingList>>(emptyList())
    val shoppingLists: StateFlow<List<ShoppingList>> = _shoppingLists.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun loadShoppingLists() {
        viewModelScope.launch {
            val result = shoppingListRepository.getShoppingLists()
            result.onSuccess { lists -> _shoppingLists.value = lists }
                .onFailure { error ->
                    _errorMessage.value = error.message
                }
        }
    }

    fun createShoppingList(
        name: String,
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {
        viewModelScope.launch {
            val result = shoppingListRepository.createShoppingList(name)
            result.onSuccess {
                loadShoppingLists()
                onSuccess()
            }.onFailure { error ->
                _errorMessage.value = error.message
                onError()
            }
        }
    }
}
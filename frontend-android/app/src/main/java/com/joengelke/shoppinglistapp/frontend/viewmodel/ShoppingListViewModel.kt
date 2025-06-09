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
class ShoppingListViewModel @Inject constructor(
    private val shoppingListRepository: ShoppingListRepository,
) : ViewModel() {


    private val _shoppingLists = MutableStateFlow<List<ShoppingList>>(emptyList())
    val shoppingLists: StateFlow<List<ShoppingList>> = _shoppingLists.asStateFlow()

    // list of unchecked items of each shoppingList in shoppingLists: String shoppingListId and Int amount
    private val _uncheckedItemsAmount = MutableStateFlow<Map<String, Int>>(emptyMap())
    val uncheckedItemsAmount: StateFlow<Map<String, Int>> = _uncheckedItemsAmount

    // Admin settings
    private val _allShoppingLists = MutableStateFlow<List<ShoppingList>>(emptyList())
    val allShoppingLists: StateFlow<List<ShoppingList>> = _allShoppingLists.asStateFlow()

    fun loadShoppingLists(
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        viewModelScope.launch {
            val result = shoppingListRepository.getShoppingLists()
            result.onSuccess { lists ->
                _shoppingLists.value = lists
                onSuccess()
            }
            result.onFailure {
                onFailure()
            }
        }
    }

    fun loadAllShoppingLists(
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val result = shoppingListRepository.getAllShoppingLists()
            result.onSuccess { lists ->
                _allShoppingLists.value = lists
                onSuccess()
            }
        }
    }

    fun loadUncheckedItemsAmount() {
        viewModelScope.launch {
            val result = shoppingListRepository.getUncheckedItemsAmountList()
            result.onSuccess { map ->
                _uncheckedItemsAmount.value = map
            }
        }
    }

    fun createShoppingList(
        name: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val result = shoppingListRepository.createShoppingList(name)
            result.onSuccess { shoppingList ->
                _shoppingLists.value += shoppingList
                onSuccess()
            }
        }
    }

    fun updateShoppingList(shoppingList: ShoppingList) {
        viewModelScope.launch {
            val result = shoppingListRepository.updateShoppingList(shoppingList)
            result.onSuccess { updatedShoppingList ->
                _shoppingLists.value = _shoppingLists.value.map {
                    if (it.id == updatedShoppingList.id) updatedShoppingList else it
                }
            }
        }
    }

    fun deleteShoppingList(shoppingListId: String) {
        viewModelScope.launch {
            val result = shoppingListRepository.deleteShoppingList(shoppingListId)
            result.onSuccess {
                _shoppingLists.value = _shoppingLists.value.filter { it.id != shoppingListId }
                _allShoppingLists.value = _allShoppingLists.value.filter { it.id != shoppingListId }
            }
        }
    }
}
package com.joengelke.shoppinglistapp.frontend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joengelke.shoppinglistapp.frontend.models.ShoppingItem
import com.joengelke.shoppinglistapp.frontend.models.ShoppingItemCreateRequest
import com.joengelke.shoppinglistapp.frontend.repository.ShoppingItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShoppingItemsViewModel @Inject constructor(private val shoppingItemRepository: ShoppingItemRepository,
    ) :
    ViewModel() {
    private val _shoppingItems = MutableStateFlow<List<ShoppingItem>>(emptyList())
    val shoppingItems: StateFlow<List<ShoppingItem>> = _shoppingItems.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun loadShoppingItems(
        shoppingListId: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val result = shoppingItemRepository.getItemsByShoppingList(shoppingListId)
            result.onSuccess { items ->
                _shoppingItems.value = items
                onSuccess()
            }
                .onFailure { error ->
                    _errorMessage.value = error.message
                }
        }
    }

    fun addShoppingItem(
        shoppingListId: String,
        shoppingItem: ShoppingItemCreateRequest
    ) {
        viewModelScope.launch {
            val result = shoppingItemRepository.addItemToShoppingList(shoppingListId, shoppingItem)
            result.onSuccess { newItem ->
                _shoppingItems.value += newItem
            }.onFailure { error ->
                _errorMessage.value = error.message
            }
        }
    }


    fun updateCheckedStatus(itemId: String, checked: Boolean) {
        viewModelScope.launch {
            val result = shoppingItemRepository.updateCheckedStatus(itemId, checked)
            result.onSuccess { updatedItem ->
                _shoppingItems.value = _shoppingItems.value.map {
                    if (it.id == updatedItem.id) updatedItem else it
                }

            }
        }
    }


}
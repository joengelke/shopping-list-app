package com.joengelke.shoppinglistapp.frontend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joengelke.shoppinglistapp.frontend.models.ShoppingItem
import com.joengelke.shoppinglistapp.frontend.repository.ShoppingItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShoppingItemsViewModel @Inject constructor(
    private val shoppingItemRepository: ShoppingItemRepository,
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
            }.onFailure { error ->
                _errorMessage.value = error.message
            }
        }
    }

    fun addOneShoppingItem(
        shoppingListId: String,
        shoppingItemName: String
    ) {
        viewModelScope.launch {
            val result =
                shoppingItemRepository.addOneItemToShoppingList(shoppingListId, shoppingItemName)
            result.onSuccess { updatedItem ->
                // updates shoppingItems with updatedItem via name, maybe later better with id
                _shoppingItems.update { currentList ->
                    if (currentList.any { it.name == updatedItem.name }) {
                        currentList.map { oldItem ->
                            if (oldItem.name == updatedItem.name) updatedItem else oldItem
                        }
                    } else {
                        currentList + updatedItem
                    }
                }
            }.onFailure { error ->
                _errorMessage.value = error.message
            }
        }
    }

    fun removeOneShoppingItem(
        shoppingListId: String,
        shoppingItemId: String
    ) {
        viewModelScope.launch {
            val result =
                shoppingItemRepository.removeOneItemOfShoppingList(shoppingListId, shoppingItemId)
            result.onSuccess { updatedItem ->
                if (updatedItem == null) {
                    // If updatedItem is null, item has been deleted
                    _shoppingItems.update { currentList ->
                        currentList.filter { it.id != shoppingItemId }
                    }
                } else {
                    _shoppingItems.update { currentList ->
                        currentList.map {
                            if (it.id == updatedItem.id) updatedItem else it
                        }
                    }
                }
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

    fun updateItem(updatedItem: ShoppingItem) {
        viewModelScope.launch {
            val result = shoppingItemRepository.updateItem(updatedItem)
            result.onSuccess { updatedItem ->
                _shoppingItems.value = _shoppingItems.value.map {
                    if (it.id == updatedItem.id) updatedItem else it
                }
            }
        }
    }

    fun deleteItem(shoppingListId: String, shoppingItemId: String) {
        viewModelScope.launch {
            val result = shoppingItemRepository.deleteItem(shoppingListId, shoppingItemId)
            result.onSuccess {
                _shoppingItems.value = _shoppingItems.value.filter { it.id != shoppingItemId }
            }
        }

    }


}
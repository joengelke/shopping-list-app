package com.joengelke.shoppinglistapp.frontend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joengelke.shoppinglistapp.frontend.models.ItemSetItem
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
) : ViewModel() {

    private val _shoppingItems = MutableStateFlow<List<ShoppingItem>>(emptyList())
    val shoppingItems: StateFlow<List<ShoppingItem>> = _shoppingItems.asStateFlow()

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
        }
    }

    fun addOneShoppingItem(
        shoppingListId: String,
        shoppingItem: ShoppingItem
    ) {
        viewModelScope.launch {
            val result =
                shoppingItemRepository.addOneItemToShoppingList(shoppingListId, shoppingItem)
            result.onSuccess { updatedItem ->
                // updates shoppingItems with updatedItem via name, maybe later better with id
                _shoppingItems.update { currentList ->
                    if (currentList.any { it.id == updatedItem.id }) {
                        currentList.map { oldItem ->
                            if (oldItem.id == updatedItem.id) updatedItem else oldItem
                        }
                    } else {
                        currentList + updatedItem
                    }
                }
            }
        }
    }

    fun removeOneShoppingItem(
        shoppingItemId: String
    ) {
        viewModelScope.launch {
            val result =
                shoppingItemRepository.removeOneItemOfShoppingList(shoppingItemId)
            result.onSuccess { updatedItem ->
                _shoppingItems.update { currentList ->
                    currentList.map {
                        if (it.id == updatedItem.id) updatedItem else it
                    }
                }
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

    fun updateItem(shoppingItem: ShoppingItem) {
        viewModelScope.launch {
            val result = shoppingItemRepository.updateItem(shoppingItem)
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

    /*
    ITEM SET METHODS
     */
    fun addItemSetItemToShoppingList(
        itemSetItem: ItemSetItem
    ) {
        viewModelScope.launch {
            val result = shoppingItemRepository.addItemSetItemToShoppingList(itemSetItem)
            result.onSuccess { updatedItem ->
                _shoppingItems.value = _shoppingItems.value.map {
                    if (it.id == updatedItem.id) updatedItem else it
                }
            }
        }
    }

    fun addAllItemSetItemsToShoppingList(
        itemSetId: String
    ) {
        viewModelScope.launch {
            val result = shoppingItemRepository.addAllItemSetItemsToShoppingList(itemSetId)
            result.onSuccess { updatedItems ->
                val updatedMap = updatedItems.associateBy { it.id }
                _shoppingItems.value = _shoppingItems.value.map { item ->
                    updatedMap[item.id] ?: item
                }
            }
        }
    }

    fun removeItemSetItemFromShoppingList(itemSetItem: ItemSetItem) {
        viewModelScope.launch {
            val result = shoppingItemRepository.removeItemSetItemFromShoppingList(itemSetItem)
            result.onSuccess { updatedItem ->
                _shoppingItems.value = _shoppingItems.value.map {
                    if (it.id == updatedItem.id) updatedItem else it
                }
            }
        }
    }

    fun removeAllItemSetItemsFromShoppingList(itemSetId: String) {
        viewModelScope.launch {
            val result = shoppingItemRepository.removeAllItemSetItemsFromShoppingList(itemSetId)
            result.onSuccess { updatedItems ->
                val updatedMap = updatedItems.associateBy { it.id }
                _shoppingItems.value = _shoppingItems.value.map { item ->
                    updatedMap[item.id] ?: item
                }
            }
        }
    }
}
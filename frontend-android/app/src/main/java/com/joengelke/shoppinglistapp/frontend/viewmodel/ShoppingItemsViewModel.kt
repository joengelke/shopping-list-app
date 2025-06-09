package com.joengelke.shoppinglistapp.frontend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joengelke.shoppinglistapp.frontend.models.ItemSetItem
import com.joengelke.shoppinglistapp.frontend.models.ShoppingItem
import com.joengelke.shoppinglistapp.frontend.repository.SettingsRepository
import com.joengelke.shoppinglistapp.frontend.repository.ShoppingItemRepository
import com.joengelke.shoppinglistapp.frontend.ui.common.ShoppingItemsSortCategory
import com.joengelke.shoppinglistapp.frontend.ui.common.SortDirection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.Collator
import java.time.Instant
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ShoppingItemsViewModel @Inject constructor(
    private val shoppingItemRepository: ShoppingItemRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _shoppingItems = MutableStateFlow<List<ShoppingItem>>(emptyList())
    val shoppingItems: StateFlow<List<ShoppingItem>> = _shoppingItems.asStateFlow()

    private val shoppingItemsSortOption = settingsRepository.shoppingItemsSortOptionFlow

    val sortedShoppingItems: StateFlow<List<ShoppingItem>> = combine(
        _shoppingItems,
        shoppingItemsSortOption
    ) { items, sortOption ->
        val comparator = when (sortOption.category) {
            ShoppingItemsSortCategory.ALPHABETICAL -> {
                val collator = Collator.getInstance(Locale.GERMAN)
                compareBy<ShoppingItem> { collator.getCollationKey(it.name)}
            }

            ShoppingItemsSortCategory.CHECKED_AT -> compareBy<ShoppingItem> {
                it.checkedAt
                //it.checkedAt.let(Instant::parse) ?: Instant.MIN
            }

            ShoppingItemsSortCategory.EDITED_AT -> compareBy<ShoppingItem> {
                it.editedAt.let(Instant::parse) ?: Instant.MIN
            }
        }

        if (sortOption.direction == SortDirection.ASCENDING) {
            items.sortedWith(comparator)
        } else {
            items.sortedWith(comparator.reversed())
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // undo function
    private val maxUndoSize = 5
    private val undoStack = ArrayDeque<ShoppingItem>(maxUndoSize)
    private val _isUndoAvailable = MutableStateFlow(false)
    val isUndoAvailable: StateFlow<Boolean> = _isUndoAvailable.asStateFlow()

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

    fun updateCheckedStatus(shoppingListId: String, shoppingItem: ShoppingItem, checked: Boolean) {
        viewModelScope.launch {
            val result = shoppingItemRepository.updateCheckedStatus(shoppingListId, shoppingItem.id, checked)
            result.onSuccess { updatedItem ->
                if (checked) {
                    pushToUndoStack(shoppingItem)
                }
                _shoppingItems.value = _shoppingItems.value.map {
                    if (it.id == updatedItem.id) updatedItem else it
                }
            }
        }
    }

    fun undoLastCheckedItem() {
        val lastCheckedItem = undoStack.removeLastOrNull() ?: return
        updateItem(lastCheckedItem.copy(checked = false))
        _isUndoAvailable.value = undoStack.isNotEmpty()
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

    private fun pushToUndoStack(shoppingItem: ShoppingItem) {
        if (undoStack.size >= maxUndoSize) {
            undoStack.removeFirst() // Remove oldest item
        }
        undoStack.addLast(shoppingItem)
        _isUndoAvailable.value = undoStack.isNotEmpty()
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
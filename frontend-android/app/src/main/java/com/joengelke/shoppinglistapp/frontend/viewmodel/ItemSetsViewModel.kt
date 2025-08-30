package com.joengelke.shoppinglistapp.frontend.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joengelke.shoppinglistapp.frontend.common.exception.AppException
import com.joengelke.shoppinglistapp.frontend.models.ItemSet
import com.joengelke.shoppinglistapp.frontend.models.ItemSetItem
import com.joengelke.shoppinglistapp.frontend.repository.ItemSetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ItemSetsViewModel @Inject constructor(
    private val itemSetRepository: ItemSetRepository,
) : ViewModel() {

    private val _itemSets = MutableStateFlow<List<ItemSet>>(emptyList())
    val itemSets: StateFlow<List<ItemSet>> = _itemSets.asStateFlow()

    val alphabeticSortedItemSets: StateFlow<List<ItemSet>> =
        itemSets
            .map{ sets ->
                sets.sortedBy { it.name.lowercase() }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    private val _hasUnsavedChanges = MutableStateFlow(false)
    val hasUnsavedChanges: StateFlow<Boolean> = _hasUnsavedChanges.asStateFlow()

    private val _selectedReceiptFile = mutableStateOf<File?>(null)
    private val selectedReceiptFile: State<File?> = _selectedReceiptFile

    fun loadItemSets(
        shoppingListId: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val result = itemSetRepository.getItemSetsByShoppingList(shoppingListId)
            result.onSuccess { sets ->
                _itemSets.value = sets
                onSuccess()
            }
        }
    }

    fun createEmptyItemSet(
        shoppingListId: String,
        itemSetName: String,
        onSuccess: (ItemSet) -> Unit
    ) {
        viewModelScope.launch {
            val result = itemSetRepository.createItemSet(
                shoppingListId,
                ItemSet("", itemSetName, emptyList(), ""),
                null
            )
            result.onSuccess { itemSet ->
                _itemSets.value += itemSet
                onSuccess(itemSet)
            }
        }
    }

    fun updateItemSet(
        shoppingListId: String,
        itemSet: ItemSet,
        onSuccess: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result =
                itemSetRepository.updateItemSet(shoppingListId, itemSet, selectedReceiptFile.value)
            result.onSuccess { updatedItemSet ->
                _itemSets.value = _itemSets.value.map {
                    if (it.id == updatedItemSet.id) updatedItemSet else it
                }
                onSuccess(updatedItemSet.name)
                _hasUnsavedChanges.value = false
            }
        }
    }

    fun uploadItemSet(
        shoppingListId: String,
        itemSet: ItemSet,
        onSuccess: () -> Unit = {},
        itemSetNameExists: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = itemSetRepository.createItemSet(
                shoppingListId,
                ItemSet("", itemSet.name, itemSet.itemList, itemSet.receiptFileId),
                null
            )
            result.onSuccess { uploadedItemSet ->
                _itemSets.value += uploadedItemSet
                onSuccess()
            }
            result.onFailure { e ->
                when(e) {
                    is AppException.ItemSetNameDuplicationException -> itemSetNameExists()
                }
            }
        }
    }

    fun getReceiptFile(
        itemSetId: String,
        onSuccess: (File) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) { // Run in IO dispatcher (background)
            val result = itemSetRepository.getReceiptFile(itemSetId)
            result.onSuccess { receiptFile ->
                withContext(Dispatchers.Main) { // Switch back to Main thread for UI callback
                    onSuccess(receiptFile)
                }
            }
        }
    }

    fun deleteItemSet(
        shoppingListId: String,
        itemSetId: String
    ) {
        viewModelScope.launch {
            val result = itemSetRepository.deleteItemSet(shoppingListId, itemSetId)
            result.onSuccess {
                _itemSets.value = _itemSets.value.filter { it.id != itemSetId }
            }
        }
    }

    /*
    FRONTEND ITEMSET METHODS
     */

    // add empty itemSetItem in frontend to create new container
    fun addEmptyItemSetItem(
        itemSetId: String,
        onSuccess: (String) -> Unit
    ) {
        val tmpId = UUID.randomUUID().toString()
        _itemSets.value = _itemSets.value.map { itemSet ->
            if (itemSet.id == itemSetId) {
                (itemSet.itemList ?: emptyList()).let { currentItems ->
                    itemSet.copy(itemList = currentItems + ItemSetItem(
                        id = "",
                        tmpId = tmpId,
                        name = "",
                        amount = 1.0,
                        unit = ""
                    ))
                }
            } else {
                itemSet
            }
        }
        _hasUnsavedChanges.value = true
        onSuccess(tmpId)
    }

    fun updateItemSetItem(itemSetId: String, updatedItemSetItem: ItemSetItem) {
        _itemSets.value = _itemSets.value.map { itemSet ->
            if (itemSet.id == itemSetId) {
                val updatedItems = itemSet.itemList.map { item ->
                    if (item.tmpId == updatedItemSetItem.tmpId) updatedItemSetItem else item
                }
                itemSet.copy(itemList = updatedItems)
            } else itemSet
        }
        _hasUnsavedChanges.value = true
    }

    fun setSelectedReceiptFile(file: File) {
        _selectedReceiptFile.value = file
        _hasUnsavedChanges.value = true
    }

    fun clearSelectedReceiptFile() {
        _selectedReceiptFile.value = null
    }

    fun deleteItemSetItem(itemSetId: String, itemSetItem: ItemSetItem) {
        _itemSets.value = _itemSets.value.map { itemSet ->
            if (itemSet.id == itemSetId) {
                val updatedItems = itemSet.itemList.filter {
                    it.tmpId != itemSetItem.tmpId
                }
                itemSet.copy(itemList = updatedItems)
            } else {
                itemSet
            }
        }
        _hasUnsavedChanges.value = true
    }
}
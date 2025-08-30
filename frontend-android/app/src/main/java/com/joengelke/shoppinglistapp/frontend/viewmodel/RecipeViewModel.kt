package com.joengelke.shoppinglistapp.frontend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joengelke.shoppinglistapp.frontend.models.ItemSet
import com.joengelke.shoppinglistapp.frontend.models.ItemSetItem
import com.joengelke.shoppinglistapp.frontend.models.Recipe
import com.joengelke.shoppinglistapp.frontend.models.Visibility
import com.joengelke.shoppinglistapp.frontend.repository.RecipeRepository
import com.joengelke.shoppinglistapp.frontend.repository.SettingsRepository
import com.joengelke.shoppinglistapp.frontend.ui.common.SortDirection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.Collator
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class RecipeViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    val recipes: StateFlow<List<Recipe>> = _recipes.asStateFlow()

    private val _currentRecipe = MutableStateFlow<Recipe?>(null)
    val currentRecipe = _currentRecipe.asStateFlow()

    //private val _marketplaceRecipes = MutableStateFlow<List<Recipe>>(emptyList())
    //val marketplaceRecipes: StateFlow<List<Recipe>> = _marketplaceRecipes.asStateFlow()

    val marketplaceRecipesCategories: StateFlow<List<String>> =
        recipes
            .map { recipes ->
                recipes
                    .flatMap { it.categories }
                    .distinct()
                    .sorted()
            }
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val recipesSortOption = settingsRepository.recipesSortOptionFLow

    val alphabeticSortedRecipes: StateFlow<List<Pair<String, List<Recipe>>>> =
        combine(_recipes, recipesSortOption) { recipes, sortOption ->
            val collator = Collator.getInstance(Locale.GERMAN)

            val grouped = recipes.groupBy {
                it.name.firstOrNull()?.uppercaseChar()?.toString() ?: "#"
            }

            val sortedKeys = grouped.keys.sortedWith { a, b ->
                when {
                    a == "#" -> if (sortOption.direction == SortDirection.ASCENDING) 1 else -1
                    b == "#" -> if (sortOption.direction == SortDirection.ASCENDING) -1 else 1
                    else -> {
                        val comparison = collator.compare(a, b)
                        if (sortOption.direction == SortDirection.ASCENDING) comparison else -comparison
                    }
                }
            }

            sortedKeys.map { key -> key to grouped.getValue(key) }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val categorySortedRecipes: StateFlow<List<Pair<String, List<Recipe>>>> =
        combine(_recipes, recipesSortOption) { recipes, sortOption ->
            val collator = Collator.getInstance(Locale.GERMAN)

            // Group recipes under each category (or "No category")
            val grouped = recipes
                .flatMap { recipe ->
                    if (recipe.categories.isEmpty()) {
                        listOf("No category" to recipe)
                    } else {
                        recipe.categories.map { category -> category to recipe }
                    }
                }
                .groupBy({ it.first }, { it.second })
                .mapValues { (_, recipesInCategory) ->
                    // Always sort recipes A–Z inside each category
                    recipesInCategory.sortedWith(
                        compareBy { collator.getCollationKey(it.name) }
                    )
                }

            // Sort category keys based on SortDirection
            val sortedKeys = grouped.keys.sortedWith { a, b ->
                when {
                    a == "No category" -> if (sortOption.direction == SortDirection.ASCENDING) 1 else -1
                    b == "No category" -> if (sortOption.direction == SortDirection.ASCENDING) -1 else 1
                    else -> {
                        val comparison = collator.compare(a, b)
                        if (sortOption.direction == SortDirection.ASCENDING) comparison else -comparison
                    }
                }
            }

            // Return category-to-sorted-recipes pairs
            sortedKeys.map { key -> key to grouped.getValue(key) }
        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyList()
        )

    // Admin settings
    private val _allRecipes = MutableStateFlow<List<Recipe>>(emptyList())
    val allRecipes: StateFlow<List<Recipe>> = _allRecipes.asStateFlow()

    fun setCurrentRecipe(recipeId: String) {
        viewModelScope.launch {
            val recipe = recipes.value.find { it.id == recipeId }
            _currentRecipe.value = recipe
        }
    }

    fun loadRecipes(
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val result = recipeRepository.getRecipesByUserId()
            result.onSuccess { loadedRecipes ->
                _recipes.value = loadedRecipes
                onSuccess()
            }
        }
    }

    fun loadAllRecipes(
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val result = recipeRepository.getAllRecipes()
            result.onSuccess { loadedRecipes ->
                _allRecipes.value = loadedRecipes
                onSuccess()
            }
        }
    }

    fun loadMarketplaceRecipes(
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = recipeRepository.getAllMarketplaceRecipesByUserId()
            result.onSuccess { loadedRecipes ->
                _recipes.value = loadedRecipes
                onSuccess()
            }
        }
    }

    fun createEmptyRecipe(
        recipeName: String,
        onSuccess: (String) -> Unit
    ) {
        val newRecipe = Recipe(
            id = "",
            name = recipeName,
            creatorId = "",
            createdAt = "",
            itemSet = ItemSet("", recipeName, emptyList(), ""),
            description = "",
            instructions = emptyList(),
            categories = emptyList(),
            visibility = Visibility.PRIVATE,
            sharedWithUserIds = emptyList(),
            receiptFileId = ""
        )
        viewModelScope.launch {
            val result = recipeRepository.createRecipe(newRecipe)
            result.onSuccess { recipe ->
                _recipes.update { it + recipe }
                onSuccess(recipe.id)
            }.onFailure {

            }
        }
    }

    fun convertItemSetToRecipe(
        itemSet: ItemSet,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = recipeRepository.convertItemSetToRecipe(itemSet)
            result.onSuccess {
                onSuccess()
            }.onFailure { error ->
                onFailure(error.message ?: "An unknown error occurred")
            }
        }
    }

    fun changeVisibility(
        recipeId: String,
        visibility: Visibility,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val result = recipeRepository.changeVisibility(recipeId, visibility)
            result.onSuccess { updatedRecipe ->
                _recipes.update { currentList ->
                    currentList.map { if (it.id == recipeId) updatedRecipe else it }
                }
                _currentRecipe.value = updatedRecipe
                onSuccess()
            }.onFailure {
                //TODO
            }
        }
    }

    fun updateRecipe(
        recipe: Recipe
    ) {
        viewModelScope.launch {
            // filters out empty instructions, items and steps
            val cleanedRecipe = recipe.copy(
                itemSet = recipe.itemSet.copy(
                    itemList = recipe.itemSet.itemList
                        .map { it.copy(name = it.name.trim()) }
                        .filter { it.name.isNotBlank() }
                ),
                instructions = recipe.instructions
                    .map { it.trim() }
                    .filter { it.isNotBlank() },
                categories = recipe.categories
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
            )
            val result = recipeRepository.updateRecipe(cleanedRecipe)
            result.onSuccess { updatedRecipe ->
                _recipes.update { currentList ->
                    currentList.map { if (it.id == recipe.id) updatedRecipe else it }
                }
                _currentRecipe.value = updatedRecipe
            }
        }
    }

    fun importRecipeFromUrl(
        url: String,
        onSuccess: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = when {
                "chefkoch." in url -> recipeRepository.fetchChefkochRecipe(url)
                "cookidoo." in url -> recipeRepository.fetchCookidooRecipe(url)
                else -> Result.failure(IllegalArgumentException("Unsupported recipe URL"))
            }
            result.onSuccess { recipe ->
                _recipes.update { it + recipe }
                onSuccess(recipe.id)
            }
            result.onFailure {
                //TODO
            }
        }
    }

    fun addRecipeToUser(
        recipeId: String,
        username: String?,
        updateRecipes: Boolean = true,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        viewModelScope.launch {
            val result = recipeRepository.addRecipeToUser(recipeId, username)
            result.onSuccess { updatedRecipeList ->
                if(updateRecipes) {
                    _recipes.value = updatedRecipeList
                }
                onSuccess()
            }
            result.onFailure {
                onFailure()
            }
        }
    }

    fun removeRecipeFromUser(
        recipeId: String,
        userId: String?
    ) {
        viewModelScope.launch {
            val result = recipeRepository.removeRecipeFromUser(recipeId, userId)
            result.onSuccess {
                _recipes.value = _recipes.value.filter { it.id != recipeId }
            }
            result.onFailure {
                //TODO
            }
        }
    }

    fun deleteRecipe(
        recipeId: String
    ) {
        viewModelScope.launch {
            val result = recipeRepository.deleteRecipe(recipeId)
            result.onSuccess {
                _recipes.value = _recipes.value.filter { it.id != recipeId }
                _allRecipes.value = _allRecipes.value.filter { it.id != recipeId }
            }
        }
    }


    // local changes:

    fun updateDescription(newDescription: String) {
        _currentRecipe.value = _currentRecipe.value?.copy(description = newDescription)
    }

    fun addCategory(category: String) {
        _currentRecipe.value = _currentRecipe.value?.let { recipe ->
            val updatedCategories = recipe.categories.toMutableList().apply {
                if (!contains(category)) add(category)
            }
            recipe.copy(categories = updatedCategories)
        }
    }

    fun removeCategory(index: Int) {
        _currentRecipe.value?.let { recipe ->
            if (index in recipe.categories.indices) {
                val updatedCategories = recipe.categories.toMutableList().apply {
                    removeAt(index)
                }
                _currentRecipe.value = recipe.copy(categories = updatedCategories)
            }
        }
    }

    fun updateInstructionAtIndex(index: Int, newValue: String) {
        _currentRecipe.value = _currentRecipe.value?.let { recipe ->
            val updatedInstruction = recipe.instructions.toMutableList()

            if (index in updatedInstruction.indices) {
                updatedInstruction[index] = newValue
                recipe.copy(instructions = updatedInstruction)
            } else {
                recipe
            }
        }
    }

    fun addEmptyInstruction(
        onSuccess: (Int) -> Unit
    ) {
        _currentRecipe.value = _currentRecipe.value?.let { recipe ->
            val updatedInstructions = recipe.instructions.toMutableList()
            updatedInstructions.add("") // Add an empty string as new instruction
            onSuccess(updatedInstructions.lastIndex)
            recipe.copy(instructions = updatedInstructions)
        }
    }

    fun addEmptyItemSetItem(
        onSuccess: (String) -> Unit
    ) {
        val tmpId = UUID.randomUUID().toString()
        _currentRecipe.value = _currentRecipe.value?.let { recipe ->
            recipe.copy(
                itemSet = recipe.itemSet.copy(
                    itemList = recipe.itemSet.itemList + ItemSetItem(
                        id = "",
                        tmpId = tmpId,
                        name = "",
                        amount = 0.0,
                        unit = ""
                    )
                )
            )
        }
        onSuccess(tmpId)
    }

    fun updateItemSetItemName(itemId: String, newName: String) {
        _currentRecipe.value = _currentRecipe.value?.let { recipe ->
            val updatedList = recipe.itemSet.itemList.map { item ->
                if (item.id == itemId || item.tmpId == itemId) {
                    item.copy(name = newName)
                } else item
            }
            recipe.copy(itemSet = recipe.itemSet.copy(itemList = updatedList))
        }
    }

    fun updateItemSetItemAmount(itemId: String, newValue: String) {
        val parsedAmount = newValue.toDoubleOrNull() ?: 0.0

        _currentRecipe.value = _currentRecipe.value?.let { recipe ->
            val updatedList = recipe.itemSet.itemList.map { item ->
                if (item.id == itemId || item.tmpId == itemId) {
                    item.copy(amount = parsedAmount)
                } else item
            }
            recipe.copy(itemSet = recipe.itemSet.copy(itemList = updatedList))
        }
    }

    fun updateItemSetItemUnit(itemId: String, newUnit: String) {
        _currentRecipe.value = _currentRecipe.value?.let { recipe ->
            val updatedList = recipe.itemSet.itemList.map { item ->
                if (item.id == itemId || item.tmpId == itemId) {
                    item.copy(unit = newUnit)
                } else item
            }
            recipe.copy(itemSet = recipe.itemSet.copy(itemList = updatedList))
        }
    }

    fun deleteItemSetItem(itemId: String) {
        _currentRecipe.value = _currentRecipe.value?.let { recipe ->
            val updatedList = recipe.itemSet.itemList.filterNot { item ->
                item.id == itemId || item.tmpId == itemId
            }
            recipe.copy(itemSet = recipe.itemSet.copy(itemList = updatedList))
        }
    }
}
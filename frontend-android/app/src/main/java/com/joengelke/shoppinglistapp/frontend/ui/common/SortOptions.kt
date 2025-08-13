package com.joengelke.shoppinglistapp.frontend.ui.common

interface SortCategory

enum class ShoppingItemsSortCategory: SortCategory {
    ALPHABETICAL,
    CHECKED_AT,
    EDITED_AT,
}

enum class RecipesSortCategory : SortCategory{
    ALPHABETICAL,
    CATEGORIES
}

enum class SortDirection: SortCategory {
    ASCENDING,
    DESCENDING
}

data class SortOptions<T: SortCategory>(
    val category: T,
    val direction: SortDirection
)
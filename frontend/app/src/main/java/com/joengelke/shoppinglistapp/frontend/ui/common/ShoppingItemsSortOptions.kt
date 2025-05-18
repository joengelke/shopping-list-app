package com.joengelke.shoppinglistapp.frontend.ui.common

enum class ShoppingItemsSortCategory {
    ALPHABETICAL,
    CHECKED_AT,
    EDITED_AT,
}

enum class SortDirection {
    ASCENDING,
    DESCENDING
}

data class ShoppingItemsSortOptions(
    val category: ShoppingItemsSortCategory,
    val direction: SortDirection
)
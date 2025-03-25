package com.joengelke.shoppinglistapp.frontend.models

data class ShoppingList(
    val id: String,
    val name: String,
    val createdAt: String,
    val itemIds: List<String>
)

data class ShoppingListCreateRequest(
    val name: String
)

data class ShoppingListResponse(
    val name: String,
    val createdAt: String
)

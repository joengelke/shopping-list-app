package com.joengelke.shoppinglistapp.frontend.models

data class ShoppingList(
    val id: String,
    val name: String,
    val createdAt: String,
    val itemIds: List<String>,
    val itemSetIds: List<String>,
    val userIds: List<String>
)

data class ShoppingListCreateRequest(
    val name: String
)

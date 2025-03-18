package com.joengelke.shoppinglistapp.frontend.models

import java.util.Date

data class ShoppingList(
    val id: String,
    val name: String,
    val createdAt: Date,
    val itemIds: List<String>
)

data class ShoppingListCreateRequest(
    val name: String
)

data class ShoppingListResponse(
    val name: String,
    val createdAt: String
)

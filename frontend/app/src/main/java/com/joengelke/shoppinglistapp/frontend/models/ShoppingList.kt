package com.joengelke.shoppinglistapp.frontend.models

import java.util.Date

data class ShoppingList(
    val id: String,
    val name: String,
    val createdAt: Date,
    val itemIds: List<String>
)

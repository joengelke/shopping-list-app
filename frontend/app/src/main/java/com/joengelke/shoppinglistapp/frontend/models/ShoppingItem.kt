package com.joengelke.shoppinglistapp.frontend.models

import java.util.Date

data class ShoppingItem(
    val id: String,
    val name: String,
    val category: String,
    val amount: Double,
    val unit: String,
    val checked: Boolean,
    val note: String,
    val editedAt: Date,
    val editedBy: String
)

data class ShoppingItemRequest(
    val name: String
)
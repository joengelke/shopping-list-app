package com.joengelke.shoppinglistapp.frontend.models

data class ShoppingItem(
    val id: String,
    val name: String,
    val category: String,
    val amount: Double,
    val unit: String,
    val checked: Boolean,
    val checkedAt: String,
    val note: String,
    val editedAt: String,
    val editedBy: String
)

data class DeleteResponse(
    val message: String
)
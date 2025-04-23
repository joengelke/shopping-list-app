package com.joengelke.shoppinglistapp.frontend.models

data class ItemSet (
    val id: String,
    val name: String,
    val itemList: List<ItemSetItem>
)

data class ItemSetItem (
    val id: String,
    val tmpId: String, // temporary ID for frontend use only
    val name: String,
    val amount: Double,
    val unit: String
)
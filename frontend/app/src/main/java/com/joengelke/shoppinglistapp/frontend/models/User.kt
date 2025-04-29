package com.joengelke.shoppinglistapp.frontend.models

data class User(
    val id: String,
    val username: String,
    val roles: List<String>
)

data class AddUserRequest(
    val username: String
)
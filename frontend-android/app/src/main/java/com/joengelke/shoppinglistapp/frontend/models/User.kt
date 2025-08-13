package com.joengelke.shoppinglistapp.frontend.models

data class User(
    val id: String,
    val username: String,
    val email: String,
    val roles: List<String>,
    val recipeIds: List<String>
)

data class AddUserRequest(
    val username: String
)

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

data class ChangeUsernameRequest(
    val newUsername: String
)
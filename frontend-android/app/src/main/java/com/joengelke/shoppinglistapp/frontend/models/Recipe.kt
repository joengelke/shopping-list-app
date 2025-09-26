package com.joengelke.shoppinglistapp.frontend.models

import kotlinx.serialization.Serializable

@Serializable
data class Recipe(
    val id: String,
    val name: String,
    val creatorId: String,
    val createdAt: String,
    val itemSet: ItemSet,
    val description: String,
    val instructions: List<String>,
    val categories: List<String>,
    val visibility: Visibility,
    val sharedWithUserIds: List<String>,
    val recipeFileIds: List<String>
)

enum class Visibility {
    PUBLIC,
    SHARED,
    PRIVATE
}

enum class RecipeSource {
    LOCAL,
    MARKETPLACE
}
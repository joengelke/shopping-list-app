package com.joengelke.shoppinglistapp.frontend.navigation

import com.joengelke.shoppinglistapp.frontend.models.RecipeSource

sealed class Routes(val route: String) {
    data object Login : Routes("login")
    data object Register : Routes("register")
    data object ShoppingListOverview : Routes("shoppingListOverview")
    data object ShoppingListCreate : Routes("shoppingListCreate")
    data object ShoppingListUser : Routes("shoppingListUser/{shoppingListId}") {
        fun createRoute(shoppingListId: String) = "shoppingListUser/$shoppingListId"
    }

    data object ShoppingItemsOverview :
        Routes("shoppingItemsOverview/{shoppingListName}:{shoppingListId}") {
        fun createRoute(shoppingListName: String, shoppingListId: String) =
            "shoppingItemsOverview/$shoppingListName:$shoppingListId"
    }

    data object ShoppingItemsCreate : Routes("shoppingItemsCreate/{shoppingListId}") {
        fun createRoute(shoppingListId: String) = "shoppingItemsCreate/$shoppingListId"
    }

    data object ItemSetOverview : Routes("itemSetOverview/{shoppingListId}") {
        fun createRoute(shoppingListId: String) = "itemSetOverview/$shoppingListId"
    }

    data object ItemSetCreate : Routes("itemSetCreate/{shoppingListId}:{itemSetId}") {
        fun createRoute(shoppingListId: String, itemSetId: String) =
            "itemSetCreate/$shoppingListId:$itemSetId"
    }

    data object SettingsOverview : Routes("settingsOverview")
    data object SettingsUser : Routes("settingsUser")
    data object SettingsUserUsername : Routes("settingsUserUsername")
    data object SettingsUserPassword : Routes("settingsUserPassword")
    data object SettingsAdmin : Routes("settingsAdmin")
    data object SettingsAdminUsers : Routes("settingsAdminUsers")
    data object SettingsAdminShoppingLists : Routes("settingsAdminShoppingLists")
    data object SettingsAdminShoppingItems: Routes("settingsAdminShoppingItems/{shoppingListId}") {
        fun createRoute(shoppingListId: String) = "settingsAdminShoppingItems/$shoppingListId"
    }
    data object SettingsAdminRecipes: Routes("settingsAdminRecipes")
    data object RecipesOverview: Routes("recipesOverview")
    data object RecipeView: Routes("recipeView/{recipeId}/{source}") {
        fun createRoute(recipeId: String, source: RecipeSource) = "recipeView/$recipeId/${source.name}"
    }

    data object MarketplaceOverview: Routes("marketplaceOverview")

}
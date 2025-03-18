package com.joengelke.shoppinglistapp.frontend.navigation

sealed class Routes(val route: String) {
    data object Login : Routes("login")
    data object Register : Routes("register")
    data object ShoppingListOverview : Routes("shoppingListOverview")
    data object ShoppingListCreate : Routes("shoppingListCreate")
    data object ShoppingItemsOverview : Routes("shoppingItemsOverview/{shoppingListId}") {
        fun createRoute(shoppingListId:String) = "shoppingItemsOverview/$shoppingListId"
    }
    data object ShoppingItemsCreate: Routes("shoppingItemsCreate/{shoppingListId}") {
        fun createRoute(shoppingListId:String) = "shoppingItemsCreate/$shoppingListId"

    }

}
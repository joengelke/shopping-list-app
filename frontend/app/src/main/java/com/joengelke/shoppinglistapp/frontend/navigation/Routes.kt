package com.joengelke.shoppinglistapp.frontend.navigation

sealed class Routes(val route: String) {
    object Login : Routes("login")
    object Register : Routes("register")
    object ShoppingListOverview : Routes("shoppingListOverview")
    object ShoppingListCreate : Routes("shoppingListCreate")
    object ShoppingItemsOverview : Routes("shoppingItemsOverview")

}
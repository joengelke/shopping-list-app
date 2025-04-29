package com.joengelke.shoppinglistapp.frontend.navigation

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

}
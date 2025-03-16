package com.joengelke.shoppinglistapp.frontend.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController

@Composable
fun ShoppingItemsOverviewScreen(navController: NavHostController) {

    val shoppingListId = navController.currentBackStackEntry
        ?.savedStateHandle?.get<String>("shoppingListId")

    // You can now use the shoppingListId in your logic to fetch the shopping list items
    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = "Shopping List ID: $shoppingListId")
        // Further UI or logic to show the shopping list's items based on the ID
    }
}
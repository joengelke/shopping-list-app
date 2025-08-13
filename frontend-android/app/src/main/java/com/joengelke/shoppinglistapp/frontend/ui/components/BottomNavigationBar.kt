package com.joengelke.shoppinglistapp.frontend.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.joengelke.shoppinglistapp.frontend.R

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    NavigationBar {
        // Shopping List
        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_shopping_cart_24),
                    contentDescription = "Shopping",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            label = { Text(stringResource(R.string.my_lists)) },
            selected = currentRoute == "shoppingListOverview",
            onClick = {
                if (currentRoute != "shoppingListOverview") {
                    navController.navigate("shoppingListOverview") {
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        )

        // Receipts
        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_receipt_long_24),
                    contentDescription = "Receipts",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            label = { Text(stringResource(R.string.my_recipes)) },
            selected = currentRoute == "recipesOverview",
            onClick = {
                if (currentRoute != "recipesOverview") {
                    navController.navigate("recipesOverview") {
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            enabled = true
        )

        // Marketplace
        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_store_24),
                    contentDescription = "Marketplace",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            label = { Text(stringResource(R.string.marketplace)) },
            selected = currentRoute == "marketplaceOverview",
            onClick = {
                if (currentRoute != "marketplaceOverview") {
                    navController.navigate("marketplaceOverview") {
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            enabled = true
        )
    }
}

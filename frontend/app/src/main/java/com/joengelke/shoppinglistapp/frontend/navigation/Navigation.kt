package com.joengelke.shoppinglistapp.frontend.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.joengelke.shoppinglistapp.frontend.ui.*
import com.joengelke.shoppinglistapp.frontend.viewmodel.AuthViewModel


@Composable
fun Navigation(authViewModel: AuthViewModel, navController: NavHostController) {
    //val navController = rememberNavController()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    LaunchedEffect(Unit) {
        authViewModel.checkIfTokenIsValid()
    }

    // checked for valid token to skip login, TODO: check if backend exists/is online
    val startDestination = if (isLoggedIn) Routes.ShoppingListOverview.route else Routes.Login.route

    NavHost(navController, startDestination = startDestination) {
        composable(Routes.Login.route) {
            LoginScreen(navController, authViewModel, onLoginSuccess = {
                navController.navigate(Routes.ShoppingListOverview.route) {
                    popUpTo(Routes.Login.route) {
                        inclusive = true
                    }
                }
            })
        }
        composable(Routes.Register.route) {
            RegisterScreen(navController)
        }
        composable(Routes.ShoppingListOverview.route) {
            ShoppingListOverviewScreen(navController)
        }
        composable(Routes.ShoppingListCreate.route) {
            ShoppingListCreateScreen(navController)
        }
        composable(Routes.ShoppingListUser.route) { backStackEntry ->
            val shoppingListId = backStackEntry.arguments?.getString("shoppingListId")?:""
            ShoppingListUserScreen(navController, shoppingListId)
        }
        composable(Routes.ShoppingItemsOverview.route) { backStackEntry ->
            val shoppingListName = backStackEntry.arguments?.getString("shoppingListName")?:""
            val shoppingListId = backStackEntry.arguments?.getString("shoppingListId")?:""
            ShoppingItemsOverviewScreen(navController, shoppingListName, shoppingListId)
        }
        composable(Routes.ShoppingItemsCreate.route) { backStackEntry ->
            val shoppingListId = backStackEntry.arguments?.getString("shoppingListId")?:""
            ShoppingItemsCreateScreen(navController, shoppingListId)
        }
        composable(Routes.ItemSetOverview.route) { backStackEntry ->
            val shoppingListId = backStackEntry.arguments?.getString("shoppingListId")?:""
            ItemSetOverviewScreen(navController, shoppingListId)
        }
        composable(Routes.ItemSetCreate.route) { backStackEntry ->
            val shoppingListId = backStackEntry.arguments?.getString("shoppingListId")?:""
            val itemSetId = backStackEntry.arguments?.getString("itemSetId")?:""
            ItemSetCreateScreen(navController, shoppingListId, itemSetId)
        }

    }
}
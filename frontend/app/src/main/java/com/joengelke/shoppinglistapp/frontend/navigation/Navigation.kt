package com.joengelke.shoppinglistapp.frontend.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.joengelke.shoppinglistapp.frontend.ui.*
import com.joengelke.shoppinglistapp.frontend.viewmodel.AuthViewModel


@Composable
fun Navigation(authViewModel: AuthViewModel, navController: NavHostController) {
    //val navController = rememberNavController()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    var isCheckingToken by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        authViewModel.checkIfTokenIsValid()
        isCheckingToken = false
    }

    if(isCheckingToken) {
        LoadingScreen()
        return
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

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
package com.joengelke.shoppinglistapp.frontend.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.joengelke.shoppinglistapp.frontend.ui.*
import com.joengelke.shoppinglistapp.frontend.viewmodel.AuthViewModel


@Composable
fun Navigation(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
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
        composable(Routes.ShoppingItemsOverview.route) {
            ShoppingItemsOverviewScreen(navController)
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
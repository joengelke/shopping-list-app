package com.joengelke.shoppinglistapp.frontend.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.joengelke.shoppinglistapp.frontend.ui.screens.auth.LoginScreen
import com.joengelke.shoppinglistapp.frontend.ui.screens.auth.RegisterScreen
import com.joengelke.shoppinglistapp.frontend.ui.screens.itemsets.ItemSetCreateScreen
import com.joengelke.shoppinglistapp.frontend.ui.screens.itemsets.ItemSetOverviewScreen
import com.joengelke.shoppinglistapp.frontend.ui.screens.settings.*
import com.joengelke.shoppinglistapp.frontend.ui.screens.shoppingitems.ShoppingItemsCreateScreen
import com.joengelke.shoppinglistapp.frontend.ui.screens.shoppingitems.ShoppingItemsOverviewScreen
import com.joengelke.shoppinglistapp.frontend.ui.screens.shoppinglist.ShoppingListCreateScreen
import com.joengelke.shoppinglistapp.frontend.ui.screens.shoppinglist.ShoppingListOverviewScreen
import com.joengelke.shoppinglistapp.frontend.ui.screens.shoppinglist.ShoppingListUserScreen
import com.joengelke.shoppinglistapp.frontend.viewmodel.AuthViewModel
import com.joengelke.shoppinglistapp.frontend.viewmodel.SettingsViewModel


@Composable
fun Navigation(authViewModel: AuthViewModel, settingsViewModel: SettingsViewModel, navController: NavHostController) {
    //val navController = rememberNavController()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    LaunchedEffect(Unit) {
        authViewModel.checkIfTokenIsValid()
    }

    // checked for valid token to skip login
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
            ShoppingItemsOverviewScreen(navController, shoppingListName, shoppingListId, settingsViewModel)
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
        composable(
            route = Routes.SettingsOverview.route,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
            ) {
           SettingsOverviewScreen(navController, settingsViewModel)
        }
        composable(
            route = Routes.SettingsUser.route,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
        ) {
            SettingsUserScreen(navController)
        }
        composable(
            route = Routes.SettingsUserUsername.route,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
        ) {
            SettingsUserUsernameScreen(navController)
        }
        composable(
            route = Routes.SettingsUserPassword.route,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
        ) {
            SettingsUserPasswordScreen(navController)
        }
        composable(
            route = Routes.SettingsAdmin.route,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
        ) {
            SettingsAdminScreen(navController)
        }
        composable(
            route = Routes.SettingsAdminUsers.route
        ) {
            SettingsAdminUsersScreen(navController)
        }
        composable(
            route = Routes.SettingsAdminShoppingLists.route
        ) {
            SettingsAdminShoppingListsScreen(navController)
        }
        composable(Routes.SettingsAdminShoppingItems.route) { backStackEntry ->
            val shoppingListId = backStackEntry.arguments?.getString("shoppingListId")?:""
            SettingsAdminShoppingItemsScreen(navController, shoppingListId)
        }
    }
}
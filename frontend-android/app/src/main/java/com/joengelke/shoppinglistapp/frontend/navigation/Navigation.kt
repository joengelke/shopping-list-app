package com.joengelke.shoppinglistapp.frontend.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.joengelke.shoppinglistapp.frontend.models.RecipeSource
import com.joengelke.shoppinglistapp.frontend.ui.common.LoginState
import com.joengelke.shoppinglistapp.frontend.ui.screens.auth.LoginScreen
import com.joengelke.shoppinglistapp.frontend.ui.screens.auth.RegisterScreen
import com.joengelke.shoppinglistapp.frontend.ui.screens.itemsets.ItemSetCreateScreen
import com.joengelke.shoppinglistapp.frontend.ui.screens.itemsets.ItemSetOverviewScreen
import com.joengelke.shoppinglistapp.frontend.ui.screens.marketplace.MarketplaceOverviewScreen
import com.joengelke.shoppinglistapp.frontend.ui.screens.recipes.RecipeViewScreen
import com.joengelke.shoppinglistapp.frontend.ui.screens.recipes.RecipesOverviewScreen
import com.joengelke.shoppinglistapp.frontend.ui.screens.settings.*
import com.joengelke.shoppinglistapp.frontend.ui.screens.shoppingitems.ShoppingItemsCreateScreen
import com.joengelke.shoppinglistapp.frontend.ui.screens.shoppingitems.ShoppingItemsOverviewScreen
import com.joengelke.shoppinglistapp.frontend.ui.screens.shoppinglist.ShoppingListCreateScreen
import com.joengelke.shoppinglistapp.frontend.ui.screens.shoppinglist.ShoppingListOverviewScreen
import com.joengelke.shoppinglistapp.frontend.ui.screens.shoppinglist.ShoppingListUserScreen
import com.joengelke.shoppinglistapp.frontend.viewmodel.AuthViewModel
import com.joengelke.shoppinglistapp.frontend.viewmodel.SettingsViewModel


@Composable
fun Navigation(
    authViewModel: AuthViewModel,
    settingsViewModel: SettingsViewModel,
    navController: NavHostController
) {
    val loginState by authViewModel.loginState.collectAsState()

    LaunchedEffect(Unit) {
        authViewModel.checkIfTokenIsValid()
    }

    if (loginState == LoginState.Unknown) {
        // Show a splash/loading screen or just a blank Box
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        val startDestination = when (loginState) {
            LoginState.LoggedIn -> Routes.ShoppingListOverview.route
            LoginState.LoggedOut -> Routes.Login.route
            else -> Routes.Login.route // fallback
        }
        NavHost(
            navController,
            startDestination = startDestination
        ) {
            composable(
                route = Routes.Login.route,
                enterTransition = {
                    when (initialState.destination.route) {
                        Routes.SettingsUser.route -> {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Down,
                                tween(600)
                            )
                        }

                        Routes.ShoppingListOverview.route -> {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Down,
                                tween(600)
                            )
                        }

                        else -> {
                            null
                        }

                    }
                },
                exitTransition = {
                    when (targetState.destination.route) {
                        Routes.ShoppingListOverview.route -> {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Up,
                                tween(600)
                            )
                        }

                        else -> {
                            null
                        }
                    }
                }
            ) {
                LoginScreen(navController, authViewModel, settingsViewModel, onLoginSuccess = {
                    navController.navigate(Routes.ShoppingListOverview.route) {
                        popUpTo(Routes.Login.route) {
                            inclusive = true
                        }
                    }
                })
            }
            composable(
                route = Routes.Register.route,
            ) {
                RegisterScreen(navController)
            }
            composable(
                route = Routes.ShoppingListOverview.route,
                enterTransition = {
                    when (initialState.destination.route) {
                        Routes.Login.route -> {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Up,
                                tween(600)
                            )
                        }

                        else -> {
                            null
                        }
                    }
                },
                exitTransition = {
                    when (targetState.destination.route) {
                        Routes.Login.route -> {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Down,
                                tween(600)
                            )
                        }

                        else -> {
                            null
                        }
                    }
                }
            ) {
                ShoppingListOverviewScreen(navController)
            }
            composable(
                route = Routes.ShoppingListCreate.route,
                enterTransition = {
                    when (initialState.destination.route) {
                        Routes.ShoppingListOverview.route -> {
                            scaleIn(tween(300)) + fadeIn(tween(300))
                        }

                        else -> {
                            null
                        }
                    }
                },
                exitTransition = {
                    when (targetState.destination.route) {
                        Routes.ShoppingListOverview.route -> {
                            scaleOut(tween(300)) + fadeOut(tween(300))
                        }

                        else -> {
                            null
                        }
                    }
                }
            ) {
                ShoppingListCreateScreen(navController)
            }
            composable(
                route = Routes.ShoppingListUser.route,
                enterTransition = {
                    when (initialState.destination.route) {
                        Routes.ShoppingItemsOverview.route -> {
                            scaleIn(tween(300)) + fadeIn(tween(300))
                        }

                        else -> {
                            null
                        }
                    }
                },
                exitTransition = {
                    when (targetState.destination.route) {
                        Routes.ShoppingItemsOverview.route -> {
                            scaleOut(tween(300)) + fadeOut(tween(300))
                        }

                        else -> {
                            null
                        }
                    }
                }
            ) { backStackEntry ->
                val shoppingListId = backStackEntry.arguments?.getString("shoppingListId") ?: ""
                ShoppingListUserScreen(navController, shoppingListId)
            }
            composable(
                route = Routes.ShoppingItemsOverview.route,
                enterTransition = {
                    when (initialState.destination.route) {
                        Routes.ShoppingListOverview.route -> {
                            scaleIn(tween(300)) + fadeIn(tween(300))
                        }

                        else -> {
                            null
                        }
                    }
                },
                exitTransition = {
                    when (targetState.destination.route) {
                        Routes.ShoppingListOverview.route -> {
                            scaleOut(tween(300)) + fadeOut(tween(300))
                        }

                        else -> {
                            null
                        }
                    }
                }
            ) { backStackEntry ->
                val shoppingListName = backStackEntry.arguments?.getString("shoppingListName") ?: ""
                val shoppingListId = backStackEntry.arguments?.getString("shoppingListId") ?: ""
                ShoppingItemsOverviewScreen(
                    navController,
                    shoppingListName,
                    shoppingListId,
                    settingsViewModel
                )
            }
            composable(
                route = Routes.ShoppingItemsCreate.route,
                enterTransition = {
                    when (initialState.destination.route) {
                        Routes.ShoppingItemsOverview.route -> {
                            scaleIn(tween(500)) + fadeIn(tween(500))
                        }

                        else -> {
                            null
                        }
                    }
                },
                exitTransition = {
                    when (targetState.destination.route) {
                        Routes.ShoppingItemsOverview.route -> {
                            scaleOut(tween(500)) + fadeOut(tween(500))
                        }

                        else -> {
                            null
                        }
                    }
                }
            ) { backStackEntry ->
                val shoppingListId = backStackEntry.arguments?.getString("shoppingListId") ?: ""
                ShoppingItemsCreateScreen(navController, shoppingListId)
            }
            composable(
                route = Routes.ItemSetOverview.route,
                enterTransition = {
                    when (initialState.destination.route) {
                        Routes.ShoppingItemsOverview.route -> {
                            scaleIn(tween(300)) + fadeIn(tween(300))
                        }

                        else -> {
                            null
                        }
                    }
                },
                exitTransition = {
                    when (targetState.destination.route) {
                        Routes.ShoppingItemsOverview.route -> {
                            scaleOut(tween(300)) + fadeOut(tween(300))
                        }

                        else -> {
                            null
                        }
                    }
                }
            ) { backStackEntry ->
                val shoppingListId = backStackEntry.arguments?.getString("shoppingListId") ?: ""
                ItemSetOverviewScreen(navController, shoppingListId)
            }
            composable(
                route = Routes.ItemSetCreate.route,
                enterTransition = {
                    when (initialState.destination.route) {
                        Routes.ItemSetOverview.route -> {
                            scaleIn(tween(300)) + fadeIn(tween(300))
                        }

                        else -> {
                            null
                        }
                    }
                },
                exitTransition = {
                    when (targetState.destination.route) {
                        Routes.ItemSetOverview.route -> {
                            scaleOut(tween(300)) + fadeOut(tween(300))
                        }

                        else -> {
                            null
                        }
                    }
                }
            ) { backStackEntry ->
                val shoppingListId = backStackEntry.arguments?.getString("shoppingListId") ?: ""
                val itemSetId = backStackEntry.arguments?.getString("itemSetId") ?: ""
                ItemSetCreateScreen(navController, shoppingListId, itemSetId)
            }
            composable(
                route = Routes.SettingsOverview.route,
                enterTransition = {
                    when (initialState.destination.route) {
                        Routes.ShoppingListOverview.route -> {
                            scaleIn(tween(300)) + fadeIn(tween(300))
                        }

                        else -> {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(300)
                            )
                        }
                    }
                },
                exitTransition = {
                    when (targetState.destination.route) {

                        Routes.ShoppingListOverview.route -> {
                            scaleOut(tween(300)) + fadeOut(tween(300))
                        }

                        else -> {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(300)
                            )
                        }
                    }
                }
            ) {
                SettingsOverviewScreen(navController, settingsViewModel)
            }
            composable(
                route = Routes.SettingsUser.route,
                enterTransition = {
                    when (initialState.destination.route) {
                        Routes.SettingsOverview.route -> {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(300)
                            )
                        }

                        else -> {
                            null
                        }
                    }
                },
                exitTransition = {
                    when (targetState.destination.route) {
                        Routes.SettingsOverview.route -> {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(300)
                            )
                        }

                        else -> {
                            null
                        }
                    }
                }
            ) {
                SettingsUserScreen(navController)
            }
            composable(
                route = Routes.SettingsUserUsername.route,
                enterTransition = {
                    when (initialState.destination.route) {
                        Routes.SettingsUser.route -> {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(300)
                            )
                        }

                        else -> {
                            null
                        }
                    }
                },
                exitTransition = {
                    when (targetState.destination.route) {
                        Routes.SettingsUser.route -> {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(300)
                            )
                        }

                        else -> {
                            null
                        }
                    }
                }
            ) {
                SettingsUserUsernameScreen(navController)
            }
            composable(
                route = Routes.SettingsUserPassword.route,
                enterTransition = {
                    when (initialState.destination.route) {
                        Routes.SettingsUser.route -> {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(300)
                            )
                        }

                        else -> {
                            null
                        }
                    }
                },
                exitTransition = {
                    when (targetState.destination.route) {
                        Routes.SettingsUser.route -> {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(300)
                            )
                        }

                        else -> {
                            null
                        }
                    }
                }
            ) {
                SettingsUserPasswordScreen(navController)
            }
            composable(
                route = Routes.SettingsAdmin.route,
                enterTransition = {
                    when (initialState.destination.route) {
                        Routes.SettingsOverview.route -> {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(300)
                            )
                        }

                        else -> {
                            null
                        }
                    }
                },
                exitTransition = {
                    when (targetState.destination.route) {
                        Routes.SettingsOverview.route -> {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(300)
                            )
                        }

                        else -> {
                            null
                        }
                    }
                }
            ) {
                SettingsAdminScreen(navController)
            }
            composable(
                route = Routes.SettingsAdminUsers.route,
                enterTransition = {
                    when (initialState.destination.route) {
                        Routes.SettingsAdmin.route -> {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(300)
                            )
                        }

                        else -> {
                            null
                        }
                    }
                },
                exitTransition = {
                    when (targetState.destination.route) {
                        Routes.SettingsAdmin.route -> {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(300)
                            )
                        }

                        else -> {
                            null
                        }
                    }
                }
            ) {
                SettingsAdminUsersScreen(navController)
            }
            composable(
                route = Routes.SettingsAdminShoppingLists.route,
                enterTransition = {
                    when (initialState.destination.route) {
                        Routes.SettingsAdmin.route -> {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(300)
                            )
                        }

                        else -> {
                            null
                        }
                    }
                },
                exitTransition = {
                    when (targetState.destination.route) {
                        Routes.SettingsAdmin.route -> {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(300)
                            )
                        }

                        else -> {
                            null
                        }
                    }
                }
            ) {
                SettingsAdminShoppingListsScreen(navController)
            }
            composable(Routes.SettingsAdminShoppingItems.route) { backStackEntry ->
                val shoppingListId = backStackEntry.arguments?.getString("shoppingListId") ?: ""
                SettingsAdminShoppingItemsScreen(navController, shoppingListId)
            }

            composable(Routes.SettingsAdminRecipes.route) {
                SettingsAdminRecipesScreen(navController)
            }

            composable(
                route = Routes.RecipesOverview.route,
            ) {
                RecipesOverviewScreen(navController)
            }

            composable(route = Routes.RecipeView.route) { backStackEntry ->
                val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
                val source =
                    RecipeSource.valueOf(backStackEntry.arguments?.getString("source") ?: "LOCAL")
                RecipeViewScreen(navController, recipeId, source)
            }

            composable(
                route = Routes.MarketplaceOverview.route,
            ) {
                MarketplaceOverviewScreen(navController)
            }
        }
    }
}

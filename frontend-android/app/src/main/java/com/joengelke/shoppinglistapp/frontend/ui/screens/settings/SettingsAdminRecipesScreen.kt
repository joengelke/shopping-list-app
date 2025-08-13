package com.joengelke.shoppinglistapp.frontend.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.joengelke.shoppinglistapp.frontend.R
import com.joengelke.shoppinglistapp.frontend.models.Recipe
import com.joengelke.shoppinglistapp.frontend.ui.components.AppScaffold
import com.joengelke.shoppinglistapp.frontend.ui.components.AppTopBar
import com.joengelke.shoppinglistapp.frontend.viewmodel.RecipeViewModel
import com.joengelke.shoppinglistapp.frontend.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsAdminRecipesScreen(
    navController: NavHostController,
    userViewModel: UserViewModel = hiltViewModel(),
    recipeViewModel: RecipeViewModel = hiltViewModel()
) {
    val allRecipes by recipeViewModel.allRecipes.collectAsState()

    var refreshing by remember { mutableStateOf(false) }
    val state = rememberPullToRefreshState()
    val onRefresh: () -> Unit = {
        refreshing = true
        recipeViewModel.loadAllRecipes(
            onSuccess = { refreshing = false }
        )
    }

    LaunchedEffect(Unit) {
        userViewModel.updateUserRoles(
            isAdmin = { isAdmin ->
                if (!isAdmin) {
                    navController.popBackStack()
                }
            }
        )
        onRefresh()
    }

    AppScaffold(
        topBar = {
            AppTopBar(
                title = "All Recipes",
                showNavigationIcon = true,
                navController = navController
            )
        },
        content = {paddingValues ->
            PullToRefreshBox(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                state = state,
                isRefreshing = refreshing,
                onRefresh = onRefresh
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    items(allRecipes) {recipe ->
                        RecipeAdminContainer(
                            recipe,
                            onDelete = { recipeId ->
                                recipeViewModel.deleteRecipe(recipeId)
                            }
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun RecipeAdminContainer(
    recipe: Recipe,
    onDelete: (String) -> Unit,
) {
    var showConfirmation by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!showConfirmation) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .weight(1f)
                ) {
                    Text(
                        text = recipe.name,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(
                    onClick = {
                        showConfirmation = true
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_delete_24),
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            } else {
                Text(
                    text = stringResource(R.string.are_you_sure),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .weight(1f),
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(
                    onClick = {
                        showConfirmation = false
                        onDelete(recipe.id)
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_check_24),
                        contentDescription = "confirm delete"
                    )
                }
                IconButton(
                    onClick = {
                        showConfirmation = false
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_close_24),
                        contentDescription = "decline delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
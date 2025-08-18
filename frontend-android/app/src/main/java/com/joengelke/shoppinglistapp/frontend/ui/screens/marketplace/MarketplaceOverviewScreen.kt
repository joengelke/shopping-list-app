package com.joengelke.shoppinglistapp.frontend.ui.screens.marketplace

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.joengelke.shoppinglistapp.frontend.R
import com.joengelke.shoppinglistapp.frontend.models.Recipe
import com.joengelke.shoppinglistapp.frontend.models.RecipeSource
import com.joengelke.shoppinglistapp.frontend.navigation.Routes
import com.joengelke.shoppinglistapp.frontend.ui.components.BottomNavigationBar
import com.joengelke.shoppinglistapp.frontend.viewmodel.RecipeViewModel
import com.joengelke.shoppinglistapp.frontend.viewmodel.SettingsViewModel
import com.joengelke.shoppinglistapp.frontend.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceOverviewScreen(
    navController: NavHostController,
    recipeViewModel: RecipeViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        recipeViewModel.loadMarketplaceRecipes()
    }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    var recipeSearchValue by remember { mutableStateOf("") }
    var showSearchBar by remember { mutableStateOf(false) }
    LaunchedEffect(showSearchBar) {
        if (showSearchBar) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    val marketplaceRecipes by recipeViewModel.recipes.collectAsState()
    val marketplaceRecipesCategories by recipeViewModel.marketplaceRecipesCategories.collectAsState()

    var selectedCategories by remember { mutableStateOf(setOf<String>()) }
    val sortedCategories = remember(marketplaceRecipesCategories, selectedCategories) {
        marketplaceRecipesCategories.sortedBy { category ->
            category !in selectedCategories
        }
    }

    val filteredRecipes =
        marketplaceRecipes
            .filter { recipe ->
                // Filter by search text
                val matchesSearch = recipeSearchValue.isEmpty() ||
                        recipe.name.contains(recipeSearchValue, ignoreCase = true)

                // Filter by category selection
                val matchesCategory = selectedCategories.isEmpty() ||
                        recipe.categories.any { it in selectedCategories }

                matchesSearch && matchesCategory
            }
            .sortedBy { it.name }

    var refreshing by remember { mutableStateOf(false) }
    val state = rememberPullToRefreshState()
    val onRefresh: () -> Unit = {
        refreshing = true
        recipeViewModel.loadMarketplaceRecipes(
            onSuccess = { refreshing = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (!showSearchBar) {
                        Text(
                            text = stringResource(R.string.marketplace),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        TextField(
                            value = recipeSearchValue,
                            onValueChange = {
                                recipeSearchValue = it
                            },
                            placeholder = {
                                Text(
                                    text = "Search...",
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            singleLine = true,
                            trailingIcon = {
                                if (recipeSearchValue.isNotBlank()) {
                                    IconButton(
                                        onClick = { recipeSearchValue = "" },
                                        modifier = Modifier.padding(end = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear",
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                            },
                            shape = RoundedCornerShape(50),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Search
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                disabledBorderColor = Color.Transparent,
                                errorBorderColor = Color.Transparent,
                                focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                                unfocusedTextColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                                cursorColor = MaterialTheme.colorScheme.onPrimary,
                            )
                        )
                    }

                },
                navigationIcon = {
                    if (showSearchBar) {
                        IconButton(
                            onClick = {
                                focusRequester.freeFocus()
                                keyboardController?.hide()
                                showSearchBar = false
                            }
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                },
                actions = {
                    if (!showSearchBar) {
                        IconButton(
                            onClick = {
                                showSearchBar = true
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_search_24),
                                contentDescription = "start edit mode",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
            )

        },
        bottomBar = {
            BottomNavigationBar(navController)
        },
        content = { paddingValues ->

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
                ) {
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            items(sortedCategories) { category ->
                                val isSelected = category in selectedCategories
                                AssistChip(
                                    onClick = {
                                        selectedCategories = if (isSelected) {
                                            selectedCategories - category
                                        } else {
                                            selectedCategories + category
                                        }
                                    },
                                    label = {
                                        Text(
                                            text = category,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    border = BorderStroke(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                    ),
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = if (isSelected)
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                        else
                                            MaterialTheme.colorScheme.surface,
                                        labelColor = if (isSelected)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }
                    items(filteredRecipes) { recipe ->
                        MarketplaceRecipeContainer(
                            recipe = recipe,
                            onOpenRecipe = { recipeId ->
                                navController.navigate(
                                    Routes.RecipeView.createRoute(
                                        recipeId,
                                        RecipeSource.MARKETPLACE
                                    )
                                )
                            },
                            onAddRecipeToUser = { recipeId ->
                                recipeViewModel.addRecipeToUser(
                                    recipeId,
                                    null,
                                    onSuccess = {
                                        Toast.makeText(
                                            context,
                                            "${recipe.name} added to to your recipes",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    onFailure = {
                                        Toast.makeText(
                                            context,
                                            "${recipe.name} could not be added",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            }
                        )
                    }
                }
            }

        },
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
    )
}

@Composable
fun MarketplaceRecipeContainer(
    recipe: Recipe,
    onOpenRecipe: (String) -> Unit,
    onAddRecipeToUser: (String) -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp, top = 8.dp)
            .clickable {
                onOpenRecipe(recipe.id)
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.primary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {

            Text(
                text = recipe.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .padding(20.dp)
                    .weight(1f)
            )
            IconButton(
                onClick = {
                    onAddRecipeToUser(recipe.id)
                }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_library_add_24),
                    contentDescription = "add recipe to user",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(end = 10.dp)
                )
            }
        }
    }
}
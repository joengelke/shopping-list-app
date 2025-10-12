package com.joengelke.shoppinglistapp.frontend.ui.screens.recipes

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.joengelke.shoppinglistapp.frontend.R
import com.joengelke.shoppinglistapp.frontend.models.Recipe
import com.joengelke.shoppinglistapp.frontend.models.RecipeSource
import com.joengelke.shoppinglistapp.frontend.models.Visibility
import com.joengelke.shoppinglistapp.frontend.navigation.Routes
import com.joengelke.shoppinglistapp.frontend.ui.common.RecipesSortCategory
import com.joengelke.shoppinglistapp.frontend.ui.components.AppScaffold
import com.joengelke.shoppinglistapp.frontend.ui.components.BottomNavigationBar
import com.joengelke.shoppinglistapp.frontend.ui.components.SortOptionsModal
import com.joengelke.shoppinglistapp.frontend.ui.components.VisibilityDropdown
import com.joengelke.shoppinglistapp.frontend.viewmodel.RecipeViewModel
import com.joengelke.shoppinglistapp.frontend.viewmodel.SettingsViewModel
import com.joengelke.shoppinglistapp.frontend.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipesOverviewScreen(
    navController: NavHostController,
    recipeViewModel: RecipeViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val currentUserId by userViewModel.currentUserId.collectAsState()

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

    val alphabeticSortedRecipes by recipeViewModel.alphabeticSortedRecipes.collectAsState()
    val categorySortedRecipes by recipeViewModel.categorySortedRecipes.collectAsState()
    val recipes by recipeViewModel.recipes.collectAsState()
    val filteredRecipes = recipes
        .filter { it.name.contains(recipeSearchValue, ignoreCase = true) }
        .sortedBy { it.name }

    val foldedLetterStates = remember { mutableStateMapOf<String, Boolean>() }
    val foldedCategoryStates = remember { mutableStateMapOf<String, Boolean>() }

    var refreshing by remember { mutableStateOf(false) }
    val state = rememberPullToRefreshState()
    val onRefresh: () -> Unit = {
        refreshing = true
        recipeViewModel.loadRecipes(
            onSuccess = { refreshing = false }
        )
    }

    var showSorting by remember { mutableStateOf(false) }
    val recipesSortOption by settingsViewModel.recipesSortOption.collectAsState()

    var showAddRecipeButtons by remember { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (showAddRecipeButtons) 45f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "rotation"
    )

    val focusRequesterOwn = remember { FocusRequester() }
    val focusRequesterUrl = remember { FocusRequester() }

    var showOwnRecipeInput by remember { mutableStateOf(false) }
    var recipeName by remember { mutableStateOf("") }

    var showUrlInput by remember { mutableStateOf(false) }
    var url by remember { mutableStateOf("") }

    var loadingNewRecipe by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        recipeViewModel.loadRecipes(onSuccess = {})
        userViewModel.updateCurrentUserId()
    }

    LaunchedEffect(showOwnRecipeInput, showUrlInput) {
        if (showOwnRecipeInput) {
            focusRequesterOwn.requestFocus()
            keyboardController?.show()
        }
        if (showUrlInput) {
            focusRequesterUrl.requestFocus()
            keyboardController?.show()
        }
    }

    AppScaffold(
        topBar = {
            Column() {
                TopAppBar(
                    title = {
                        if (!showSearchBar) {
                            Text(
                                text = if (!showAddRecipeButtons) {
                                    stringResource(R.string.my_recipes)
                                } else {
                                    stringResource(
                                        R.string.add_new_recipe
                                    )
                                },
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
                                        text = stringResource(R.string.search),
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
                                    unfocusedTextColor = MaterialTheme.colorScheme.onPrimary.copy(
                                        alpha = 0.8f
                                    ),
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
                            if (!showAddRecipeButtons) {
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
                                IconButton(onClick = { showSorting = true }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.baseline_sort_24),
                                        contentDescription = "sort recipes",
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }

                            IconButton(onClick = {
                                showAddRecipeButtons = !showAddRecipeButtons
                                showOwnRecipeInput = false
                                showUrlInput = false
                            }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_add_24),
                                    contentDescription = "add recipe",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.rotate(rotationAngle)
                                )
                            }
                        }

                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                )
                AnimatedVisibility(visible = showAddRecipeButtons) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(4.dp)
                    ) {
                        AnimatedContent(
                            targetState = when {
                                showOwnRecipeInput -> "own"
                                showUrlInput -> "url"
                                else -> "none"
                            },
                            transitionSpec = {
                                val slideInFromRight = slideInHorizontally { it } + fadeIn()
                                val slideInFromLeft = slideInHorizontally { -it } + fadeIn()
                                val slideOutToLeft = slideOutHorizontally { -it } + fadeOut()
                                val slideOutToRight = slideOutHorizontally { it } + fadeOut()

                                when {
                                    // Exiting input → show buttons ("none"): slide in from left, out to right
                                    targetState == "none" -> slideInFromLeft togetherWith slideOutToRight

                                    // Entering input from "none": slide in from right, out to left
                                    initialState == "none" -> slideInFromRight togetherWith slideOutToLeft

                                    // This shouldn't happen based on your logic, but safe fallback
                                    else -> slideInFromRight togetherWith slideOutToLeft
                                }
                            },
                            label = "Recipe input mode switch"
                        ) { mode ->
                            when (mode) {
                                "own" -> {
                                    Row {
                                        IconButton(
                                            onClick = {
                                                showOwnRecipeInput = false
                                            }
                                        ) {
                                            Icon(
                                                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                                contentDescription = "close url input",
                                                tint = MaterialTheme.colorScheme.onPrimary
                                            )
                                        }
                                        OutlinedTextField(
                                            value = recipeName,
                                            onValueChange = { newValue ->
                                                recipeName = newValue
                                            },
                                            placeholder = { Text(stringResource(R.string.recipe_name)) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .focusRequester(focusRequesterOwn),
                                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                                            keyboardActions = KeyboardActions(
                                                onDone = {
                                                    if (recipeName.isNotBlank()) {
                                                        loadingNewRecipe = true
                                                        recipeViewModel.createEmptyRecipe(
                                                            recipeName = recipeName.trim(),
                                                            onSuccess = { recipeId ->
                                                                loadingNewRecipe = false
                                                                showOwnRecipeInput = false
                                                                showAddRecipeButtons = false
                                                                navController.navigate(
                                                                    Routes.RecipeView.createRoute(
                                                                        recipeId,
                                                                        RecipeSource.LOCAL
                                                                    )
                                                                )
                                                            }
                                                        )
                                                    }
                                                }
                                            ),
                                            trailingIcon = {
                                                IconButton(
                                                    onClick = {
                                                        if (recipeName.isNotBlank()) {
                                                            loadingNewRecipe = true
                                                            recipeViewModel.createEmptyRecipe(
                                                                recipeName = recipeName.trim(),
                                                                onSuccess = { recipeId ->
                                                                    loadingNewRecipe = false
                                                                    showOwnRecipeInput = false
                                                                    showAddRecipeButtons = false
                                                                    navController.navigate(
                                                                        Routes.RecipeView.createRoute(
                                                                            recipeId,
                                                                            RecipeSource.LOCAL
                                                                        )
                                                                    )
                                                                }
                                                            )
                                                        }
                                                    }
                                                ) {
                                                    if (loadingNewRecipe) {
                                                        CircularProgressIndicator(
                                                            modifier = Modifier.size(24.dp), // match icon size
                                                            strokeWidth = 2.dp,
                                                            color = MaterialTheme.colorScheme.onPrimary
                                                        )
                                                    } else {
                                                        Icon(
                                                            painter = painterResource(id = R.drawable.baseline_add_24),
                                                            contentDescription = "create new recipe",
                                                            tint = MaterialTheme.colorScheme.onPrimary
                                                        )
                                                    }
                                                }
                                            },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                cursorColor = MaterialTheme.colorScheme.onPrimary,
                                                focusedBorderColor = MaterialTheme.colorScheme.onPrimary,
                                                focusedPlaceholderColor = MaterialTheme.colorScheme.onPrimary,
                                                focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                                                unfocusedBorderColor = MaterialTheme.colorScheme.onPrimary.copy(
                                                    alpha = 0.8f
                                                ),
                                                unfocusedPlaceholderColor = MaterialTheme.colorScheme.onPrimary.copy(
                                                    alpha = 0.8f
                                                ),
                                                unfocusedTextColor = MaterialTheme.colorScheme.onPrimary.copy(
                                                    alpha = 0.8f
                                                )
                                            )
                                        )
                                    }
                                }

                                "url" -> {
                                    Row {
                                        IconButton(
                                            onClick = {
                                                showUrlInput = false
                                            }
                                        ) {
                                            Icon(
                                                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                                contentDescription = "close url input",
                                                tint = MaterialTheme.colorScheme.onPrimary
                                            )
                                        }
                                        OutlinedTextField(
                                            value = url,
                                            onValueChange = { newValue ->
                                                url = newValue
                                            },
                                            placeholder = { Text("URL") },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .focusRequester(focusRequesterUrl),
                                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                                            keyboardActions = KeyboardActions(
                                                onDone = {
                                                    if (url.isNotBlank()) {
                                                        loadingNewRecipe = true
                                                        recipeViewModel.importRecipeFromUrl(
                                                            url,
                                                            onSuccess = { recipeId ->
                                                                loadingNewRecipe = false
                                                                showUrlInput = false
                                                                showAddRecipeButtons = false
                                                                navController.navigate(
                                                                    Routes.RecipeView.createRoute(
                                                                        recipeId,
                                                                        RecipeSource.LOCAL
                                                                    )
                                                                )
                                                            }
                                                            // TODO onFailure
                                                        )

                                                    }
                                                }
                                            ),
                                            trailingIcon = {
                                                IconButton(
                                                    onClick = {
                                                        if (url.isNotBlank()) {
                                                            loadingNewRecipe = true
                                                            recipeViewModel.importRecipeFromUrl(
                                                                url,
                                                                onSuccess = { recipeId ->
                                                                    loadingNewRecipe = false
                                                                    showUrlInput = false
                                                                    showAddRecipeButtons = false
                                                                    navController.navigate(
                                                                        Routes.RecipeView.createRoute(
                                                                            recipeId,
                                                                            RecipeSource.LOCAL
                                                                        )
                                                                    )
                                                                }
                                                                // TODO onFailure
                                                            )

                                                        }
                                                    }
                                                ) {
                                                    if (loadingNewRecipe) {
                                                        CircularProgressIndicator(
                                                            modifier = Modifier.size(24.dp), // match icon size
                                                            strokeWidth = 2.dp,
                                                            color = MaterialTheme.colorScheme.onPrimary
                                                        )
                                                    } else {
                                                        Icon(
                                                            painter = painterResource(id = R.drawable.baseline_upload_24),
                                                            contentDescription = "upload from url",
                                                            tint = MaterialTheme.colorScheme.onPrimary
                                                        )
                                                    }
                                                }
                                            },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                cursorColor = MaterialTheme.colorScheme.onPrimary,
                                                focusedBorderColor = MaterialTheme.colorScheme.onPrimary,
                                                focusedPlaceholderColor = MaterialTheme.colorScheme.onPrimary,
                                                focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                                                unfocusedBorderColor = MaterialTheme.colorScheme.onPrimary.copy(
                                                    alpha = 0.8f
                                                ),
                                                unfocusedPlaceholderColor = MaterialTheme.colorScheme.onPrimary.copy(
                                                    alpha = 0.8f
                                                ),
                                                unfocusedTextColor = MaterialTheme.colorScheme.onPrimary.copy(
                                                    alpha = 0.8f
                                                )
                                            )
                                        )
                                    }
                                }

                                else -> {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextButton(
                                            onClick = {
                                                showOwnRecipeInput = true
                                            },
                                            modifier = Modifier
                                                .weight(1f),
                                            shape = RoundedCornerShape(6.dp),
                                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onPrimary)
                                        ) {
                                            Text(stringResource(R.string.own))
                                            Icon(
                                                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                                contentDescription = "open own recipe input"
                                            )
                                        }
                                        VerticalDivider(
                                            modifier = Modifier
                                                .height(42.dp) // set exact height of the divider
                                                .padding(horizontal = 4.dp), // optional spacing between buttons
                                            thickness = 1.dp,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                        TextButton(
                                            onClick = {
                                                showUrlInput = true
                                            },
                                            modifier = Modifier
                                                .weight(1f),
                                            shape = RoundedCornerShape(6.dp),
                                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onPrimary)
                                        ) {
                                            Text("Chefkoch")
                                            Icon(
                                                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                                contentDescription = "open url input"
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            BottomNavigationBar(
                navController = navController
            )
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
                    if (!showSearchBar) {
                        if (recipesSortOption.category == RecipesSortCategory.CATEGORIES) {
                            items(categorySortedRecipes) { (category, recipeList) ->
                                val folded = foldedCategoryStates[category] ?: true
                                CategoryContainer(
                                    category,
                                    currentUserId,
                                    recipeList,
                                    categoryFolded = folded,
                                    onFoldedChange = { foldedCategoryStates[category] = it },
                                    onOpenRecipe = { recipeId ->
                                        navController.navigate(
                                            Routes.RecipeView.createRoute(
                                                recipeId,
                                                RecipeSource.LOCAL
                                            )
                                        )
                                    },
                                    onEditRecipe = { recipe ->
                                        recipeViewModel.updateRecipe(recipe)
                                    },
                                    onDeleteComplete = { recipeId ->
                                        recipeViewModel.deleteRecipe(recipeId)
                                    },
                                    onRemoveFromUser = { recipeId ->
                                        recipeViewModel.removeRecipeFromUser(recipeId, null)
                                    }
                                )
                            }
                        } else {
                            items(alphabeticSortedRecipes) { (letter, recipe) ->
                                val folded = foldedLetterStates[letter] ?: true
                                LetterContainer(
                                    letter,
                                    recipe,
                                    currentUserId,
                                    letterFolded = folded,
                                    onFoldedChange = { foldedLetterStates[letter] = it },
                                    onOpenRecipe = { recipeId ->
                                        navController.navigate(
                                            Routes.RecipeView.createRoute(
                                                recipeId,
                                                RecipeSource.LOCAL
                                            )
                                        )
                                    },
                                    onEditRecipe = { recipe ->
                                        recipeViewModel.updateRecipe(recipe)
                                    },
                                    onDeleteComplete = { recipeId ->
                                        recipeViewModel.deleteRecipe(recipeId)
                                    },
                                    onRemoveFromUser = { recipeId ->
                                        recipeViewModel.removeRecipeFromUser(recipeId, null)
                                    }
                                )
                            }
                            item {
                                Spacer(
                                    modifier = Modifier.height(8.dp)
                                )
                            }
                        }
                    } else {
                        items(filteredRecipes) { recipe ->
                            RecipeContainer(
                                recipe,
                                currentUserId,
                                onOpenRecipe = { recipeId ->
                                    navController.navigate(
                                        Routes.RecipeView.createRoute(
                                            recipeId,
                                            RecipeSource.LOCAL
                                        )
                                    )
                                },
                                onEditRecipe = { recipe ->
                                    recipeViewModel.updateRecipe(recipe)
                                },
                                onDeleteComplete = { recipeId ->
                                    recipeViewModel.deleteRecipe(recipeId)
                                },
                                onRemoveFromUser = { recipeId ->
                                    recipeViewModel.removeRecipeFromUser(recipeId, null)
                                }
                            )
                        }
                    }
                }
            }
            if (showSorting) {
                SortOptionsModal(
                    title = stringResource(R.string.sorted_by),
                    currentSortOption = recipesSortOption,
                    availableCategories = RecipesSortCategory.entries,
                    getCategoryLabel = {
                        when (it) {
                            RecipesSortCategory.ALPHABETICAL -> stringResource(R.string.alphabetical)
                            RecipesSortCategory.CATEGORIES -> stringResource(R.string.categories)
                        }
                    },
                    onChooseSortOption = { updatedOption ->
                        settingsViewModel.setRecipesSortOption(updatedOption)
                    },
                    onDismiss = { showSorting = false }
                )
            }
        }
    )
}

@Composable
fun LetterContainer(
    letter: String,
    recipeList: List<Recipe>,
    currentUserId: String,
    letterFolded: Boolean,
    onFoldedChange: (Boolean) -> Unit,
    onOpenRecipe: (String) -> Unit,
    onEditRecipe: (Recipe) -> Unit,
    onDeleteComplete: (String) -> Unit,
    onRemoveFromUser: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onFoldedChange(!letterFolded) }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = letter,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (!letterFolded) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Toggle recipes",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
            thickness = 1.dp
        )
    }
    if (!letterFolded) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            recipeList.forEach { recipe ->
                RecipeContainer(
                    recipe,
                    currentUserId,
                    onOpenRecipe = { recipeId ->
                        onOpenRecipe(recipeId)
                    },
                    onEditRecipe = { recipe ->
                        onEditRecipe(recipe)
                    },
                    onDeleteComplete = { recipeId ->
                        onDeleteComplete(recipeId)
                    },
                    onRemoveFromUser = { recipeId ->
                        onRemoveFromUser(recipeId)
                    }
                )
            }
        }
    }
}

@Composable
fun CategoryContainer(
    category: String,
    currentUserId: String,
    recipeList: List<Recipe>,
    categoryFolded: Boolean,
    onFoldedChange: (Boolean) -> Unit,
    onOpenRecipe: (String) -> Unit,
    onEditRecipe: (Recipe) -> Unit,
    onDeleteComplete: (String) -> Unit,
    onRemoveFromUser: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onFoldedChange(!categoryFolded) }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (category == "No category") stringResource(R.string.no_category) else category,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (!categoryFolded) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Toggle recipes",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
            thickness = 1.dp
        )
    }
    if (!categoryFolded) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            recipeList.forEach { recipe ->
                RecipeContainer(
                    recipe,
                    currentUserId,
                    onOpenRecipe = { recipeId ->
                        onOpenRecipe(recipeId)
                    },
                    onEditRecipe = { recipe ->
                        onEditRecipe(recipe)
                    },
                    onDeleteComplete = { recipeId ->
                        onDeleteComplete(recipeId)
                    },
                    onRemoveFromUser = { recipeId ->
                        onRemoveFromUser(recipeId)
                    }
                )
            }
        }
    }
}

@Composable
fun RecipeContainer(
    recipe: Recipe,
    currentUserId: String,
    onOpenRecipe: (String) -> Unit,
    onEditRecipe: (Recipe) -> Unit,
    onDeleteComplete: (String) -> Unit,
    onRemoveFromUser: (String) -> Unit
) {
    var editMode by remember { mutableStateOf(false) }
    var delete by remember { mutableStateOf(false) }
    var editRecipeName by remember { mutableStateOf(false) }
    var newRecipeName by remember { mutableStateOf(recipe.name) }

    LaunchedEffect(editMode) {
        if (!editMode) {
            delete = false
            editRecipeName = false
        }
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .combinedClickable(
                onClick = {
                    if (!editMode) {
                        onOpenRecipe(recipe.id)
                    }
                },
                onLongClick = {
                    editMode = true
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.primary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 16.dp,
                    end = 6.dp,
                    top = if (editRecipeName) 4.dp else 8.dp,
                    bottom = if (editRecipeName) 4.dp else 8.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (delete && editMode) {
                Text(
                    text = stringResource(R.string.are_you_sure),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(onClick = {
                    delete = false
                    if (currentUserId == recipe.creatorId) {
                        onDeleteComplete(recipe.id)
                    } else {
                        onRemoveFromUser(recipe.id)
                    }
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_check_24),
                        contentDescription = "confirm delete",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(onClick = { delete = false }) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_close_24),
                        contentDescription = "decline delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            } else if (editRecipeName && editMode) {
                OutlinedTextField(
                    value = newRecipeName,
                    onValueChange = {
                        newRecipeName = it
                    },
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = {
                        onEditRecipe(
                            recipe.copy(name = newRecipeName),
                        )
                        editRecipeName = false
                        editMode = false
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_check_24),
                        contentDescription = "save recipe name"
                    )
                }
                IconButton(
                    onClick = {
                        editRecipeName = false
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_close_24),
                        contentDescription = "cancel edit",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = recipe.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            if (currentUserId == recipe.creatorId) {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_person_24),
                                    contentDescription = "Own recipe",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        if (recipe.creatorUsername.isNotEmpty()) {
                            Text(
                                text = stringResource(R.string.by) + recipe.creatorUsername
                            )
                        }
                    }
                }
                if (editMode) {
                    if (currentUserId == recipe.creatorId) {
                        IconButton(onClick = {
                            editRecipeName = true
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_edit_24),
                                contentDescription = "edit recipe"
                            )
                        }
                        VisibilityDropdown(
                            recipeId = recipe.id,
                            visibility = recipe.visibility,
                            iconColor = MaterialTheme.colorScheme.primary,
                            onChange = {
                                editMode = false
                            }
                        )
                    }

                    IconButton(onClick = { delete = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_delete_24),
                            contentDescription = "delete recipe",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }

                    IconButton(onClick = { editMode = false }) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_check_24),
                            contentDescription = "stop edit mode"
                        )
                    }
                } else {
                    Icon(
                        painter = painterResource(id = if (recipe.visibility == Visibility.PRIVATE) R.drawable.baseline_lock_24 else R.drawable.baseline_public_24),
                        contentDescription = "visibility state",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(end = 10.dp)
                    )
                }
            }
        }
    }
}
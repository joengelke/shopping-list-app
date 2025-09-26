package com.joengelke.shoppinglistapp.frontend.ui.screens.recipes

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.joengelke.shoppinglistapp.frontend.R
import com.joengelke.shoppinglistapp.frontend.models.RecipeSource
import com.joengelke.shoppinglistapp.frontend.navigation.Routes
import com.joengelke.shoppinglistapp.frontend.ui.components.AppScaffold
import com.joengelke.shoppinglistapp.frontend.ui.components.AppTopBar
import com.joengelke.shoppinglistapp.frontend.ui.components.UnitDropdown
import com.joengelke.shoppinglistapp.frontend.utils.FileUtils
import com.joengelke.shoppinglistapp.frontend.utils.NotificationUtils
import com.joengelke.shoppinglistapp.frontend.viewmodel.ItemSetsViewModel
import com.joengelke.shoppinglistapp.frontend.viewmodel.RecipeViewModel
import com.joengelke.shoppinglistapp.frontend.viewmodel.ShoppingListViewModel
import com.joengelke.shoppinglistapp.frontend.viewmodel.UserViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeViewScreen(
    navController: NavHostController,
    recipeId: String,
    source: RecipeSource,
    recipeViewModel: RecipeViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel(),
    shoppingListViewModel: ShoppingListViewModel = hiltViewModel(),
    itemSetsViewModel: ItemSetsViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    LaunchedEffect(source, recipeId) {
        //either loads marketplace recipes or personal recipes depending on the source
        when (source) {
            RecipeSource.LOCAL -> {
                recipeViewModel.loadRecipes(
                    onSuccess = {
                        recipeViewModel.setCurrentRecipe(recipeId)
                        recipeViewModel.clearSelectedRecipeFiles()
                    }
                )
            }

            RecipeSource.MARKETPLACE -> {
                recipeViewModel.loadMarketplaceRecipes(
                    onSuccess = {
                        recipeViewModel.setCurrentRecipe(recipeId)
                    }
                )
            }
        }
        userViewModel.updateCurrentUserId()
        shoppingListViewModel.loadShoppingLists()
    }

    val recipe by recipeViewModel.currentRecipe.collectAsState()
    val currentUserId by userViewModel.currentUserId.collectAsState()
    val token by recipeViewModel.token.collectAsState()
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .okHttpClient(recipeViewModel.okHttpClient) // use injected client
            .crossfade(true)
            .build()
    }
    val selectedFiles by recipeViewModel.selectedRecipeFiles.collectAsState()

    var editMode by remember { mutableStateOf(false) }

    var openShareOptions by remember { mutableStateOf(false) }
    var currentShareMenu by remember { mutableStateOf("main") }
    val shoppingLists by shoppingListViewModel.shoppingLists.collectAsState()

    var showDescription by remember { mutableStateOf(true) }
    var editDescription by remember { mutableStateOf(false) }

    var showCategories by remember { mutableStateOf(true) }
    var editCategories by remember { mutableStateOf(false) }
    var openAddCategoryField by remember { mutableStateOf(false) }
    var categoryInput by remember { mutableStateOf("") }
    val focusRequesterCategory = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(openAddCategoryField) {
        if (openAddCategoryField) {
            focusRequesterCategory.requestFocus()
            keyboardController?.show()
        }
    }

    var showIngredients by remember { mutableStateOf(true) }
    var editIngredients by remember { mutableStateOf(false) }
    var focusIngredientId by remember { mutableStateOf<String?>(null) }

    var showInstructions by remember { mutableStateOf(true) }
    var editInstructions by remember { mutableStateOf(false) }
    var focusInstructionIndex by remember { mutableStateOf<Int?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),
        onResult = { uris ->
            uris.let { uriList ->
                val files = uriList.mapNotNull { uri ->
                    try {
                        val fileName = getFileNameFromUri(context, uri) ?: return@mapNotNull null
                        val tempFile = File(context.cacheDir, fileName)
                        context.contentResolver.openInputStream(uri)?.use { input ->
                            tempFile.outputStream().use { output -> input.copyTo(output) }
                        }
                        tempFile
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }
                recipeViewModel.addSelectedRecipeFiles(files)
            }
        }
    )

    var showDialog by remember { mutableStateOf(false) }

    AppScaffold(
        topBar = {
            Column() {
                AppTopBar(
                    title = recipe?.name ?: stringResource(R.string.loading),
                    showNavigationIcon = true,
                    onNavigationClick = {
                        if (editMode && (editDescription || editCategories || editIngredients || editInstructions)) {
                            showDialog = true
                        } else {
                            navController.popBackStack()
                        }
                    },
                    navController = navController,
                    actions = {
                        recipe?.let { recipe ->
                            if (currentUserId == recipe.creatorId) {
                                IconButton(
                                    onClick = {
                                        if (editMode) {
                                            recipeViewModel.updateRecipe(recipe)
                                            editMode = false
                                            editDescription = false
                                            editCategories = false
                                            editIngredients = false
                                            editInstructions = false
                                        } else {
                                            editMode = true
                                        }
                                    }
                                ) {
                                    if (!editMode) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.baseline_edit_24),
                                            contentDescription = "edit recipe",
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                    } else {
                                        Icon(
                                            painter = painterResource(id = R.drawable.baseline_check_24),
                                            contentDescription = "edit recipe",
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                            }
                            if (!editMode) {
                                Box {
                                    IconButton(
                                        onClick = {
                                            openShareOptions = !openShareOptions
                                        }
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.baseline_share_24),
                                            contentDescription = "open share options ",
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = openShareOptions,
                                        onDismissRequest = {
                                            openShareOptions = false
                                            currentShareMenu = "main"
                                        }
                                    ) {
                                        AnimatedContent(
                                            targetState = currentShareMenu,
                                            transitionSpec = {
                                                slideInHorizontally(
                                                    initialOffsetX = { fullWidth -> fullWidth },
                                                    animationSpec = tween(300)
                                                ) togetherWith slideOutHorizontally(
                                                    targetOffsetX = { fullWidth -> -fullWidth },
                                                    animationSpec = tween(300)
                                                )
                                            },
                                            label = "menuTransition"
                                        ) { menu ->
                                            when (menu) {
                                                "main" -> {
                                                    Column {
                                                        DropdownMenuItem(
                                                            text = { Text(stringResource(R.string.add_to_shopping_list)) },
                                                            onClick = {
                                                                currentShareMenu =
                                                                    "addToShoppingList"
                                                            }
                                                        )
                                                        DropdownMenuItem(
                                                            text = { Text("Download PDF") },
                                                            onClick = {
                                                                val file =
                                                                    FileUtils.createRecipePdf(
                                                                        context,
                                                                        recipe,
                                                                        true
                                                                    )
                                                                NotificationUtils.showDownloadNotification(
                                                                    context,
                                                                    file
                                                                )
                                                                currentShareMenu = "main"
                                                                openShareOptions = false
                                                            }
                                                        )
                                                        DropdownMenuItem(
                                                            text = { Text(stringResource(R.string.share)) },
                                                            onClick = {
                                                                currentShareMenu = "share"
                                                            }
                                                        )
                                                    }
                                                }

                                                "addToShoppingList" -> {
                                                    Column {
                                                        DropdownMenuItem(
                                                            text = { Text(stringResource(R.string.back)) },
                                                            onClick = { currentShareMenu = "main" },
                                                            leadingIcon = {
                                                                Icon(
                                                                    Icons.AutoMirrored.Filled.ArrowBack,
                                                                    contentDescription = "Back",
                                                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                                                )
                                                            }
                                                        )
                                                        HorizontalDivider()

                                                        shoppingLists.forEach { shoppingList ->
                                                            DropdownMenuItem(
                                                                text = {
                                                                    Text(shoppingList.name)
                                                                },
                                                                onClick = {
                                                                    itemSetsViewModel.uploadItemSet(
                                                                        shoppingList.id,
                                                                        recipe.itemSet,
                                                                        onSuccess = {
                                                                            Toast.makeText(
                                                                                context,
                                                                                context.getString(
                                                                                    R.string.added_to,
                                                                                    recipe.name,
                                                                                    shoppingList.name
                                                                                ),
                                                                                Toast.LENGTH_SHORT
                                                                            ).show()
                                                                            currentShareMenu =
                                                                                "main"
                                                                            openShareOptions = false
                                                                            navController.navigate(
                                                                                Routes.ShoppingItemsCreate.createRoute(
                                                                                    shoppingList.id
                                                                                )
                                                                            )
                                                                        },
                                                                        itemSetNameExists = {
                                                                            Toast.makeText(
                                                                                context,
                                                                                context.getString(
                                                                                    R.string.already_exists_in,
                                                                                    recipe.name,
                                                                                    shoppingList.name
                                                                                ),
                                                                                Toast.LENGTH_SHORT
                                                                            ).show()
                                                                            currentShareMenu =
                                                                                "main"
                                                                            openShareOptions = false
                                                                        }
                                                                    )
                                                                }
                                                            )
                                                        }
                                                    }
                                                }

                                                "share" -> {
                                                    Column {
                                                        DropdownMenuItem(
                                                            text = { Text("PDF") },
                                                            onClick = {
                                                                val file =
                                                                    FileUtils.createRecipePdf(
                                                                        context,
                                                                        recipe,
                                                                        false
                                                                    )
                                                                FileUtils.sharePdf(context, file)
                                                                currentShareMenu = "main"
                                                                openShareOptions = false
                                                            }
                                                        )
                                                        DropdownMenuItem(
                                                            text = { Text("Text") },
                                                            onClick = {
                                                                FileUtils.shareRecipeAsText(
                                                                    context,
                                                                    recipe
                                                                )
                                                                currentShareMenu = "main"
                                                                openShareOptions = false
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                )
            }
        },
        content = { paddingValues ->
            recipe?.let { recipe ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(paddingValues)
                ) {
                    // === Description Section ===
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDescription = !showDescription }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(R.string.description),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.primary,
                            )
                            if (editMode) {
                                IconButton(
                                    onClick = {
                                        if (!editDescription) {
                                            editDescription = true
                                            showDescription = true
                                        } else {
                                            recipeViewModel.updateRecipe(recipe)
                                            editDescription = false
                                        }
                                    },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = if (!editDescription) R.drawable.baseline_edit_24 else R.drawable.baseline_save_24),
                                        contentDescription = "Edit description",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                imageVector = if (showDescription) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Toggle description",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                            thickness = 1.dp
                        )
                    }
                    if (showDescription) {
                        if (!editDescription && recipe.description.isNotBlank()) {
                            Text(
                                text = recipe.description,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .padding(bottom = 12.dp)
                            )
                        }
                        if (editDescription) {
                            OutlinedTextField(
                                value = recipe.description,
                                onValueChange = { recipeViewModel.updateDescription(it) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .padding(bottom = 12.dp)
                            )
                        }
                    }

                    // === Categories Section ===
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCategories = !showCategories }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(R.string.categories),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.primary,
                            )
                            if (editMode) {
                                IconButton(
                                    onClick = {
                                        if (!editCategories) {
                                            editCategories = true
                                            showCategories = true
                                        } else {
                                            recipeViewModel.updateRecipe(recipe)
                                            editCategories = false
                                        }
                                    },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = if (!editCategories) R.drawable.baseline_edit_24 else R.drawable.baseline_save_24),
                                        contentDescription = "Edit categories",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                imageVector = if (showCategories) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Toggle categories",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                            thickness = 1.dp
                        )
                    }
                    if (showCategories) {
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .then(
                                    if (editCategories) {
                                        Modifier.border(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                    } else Modifier
                                )
                        ) {
                            recipe.categories.forEachIndexed { index, category ->
                                var confirmDelete by remember { mutableStateOf(false) }
                                AssistChip(
                                    onClick = {},
                                    label = {
                                        if (!confirmDelete) {
                                            Text(
                                                text = category,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        }
                                    },
                                    leadingIcon = {
                                        if (confirmDelete && editCategories) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.baseline_check_24),
                                                contentDescription = "delete category",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier
                                                    .clickable {
                                                        recipeViewModel.removeCategory(index)
                                                        confirmDelete = false
                                                    }
                                                    .weight(1f)
                                            )
                                        }
                                    },
                                    trailingIcon = {
                                        if (editCategories) {
                                            if (!confirmDelete) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.baseline_delete_24),
                                                    contentDescription = "delete category",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.clickable {
                                                        confirmDelete = true
                                                    }
                                                )
                                            } else {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.baseline_close_24),
                                                    contentDescription = "confirm delete category",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier
                                                        .clickable {
                                                            confirmDelete = false
                                                        }
                                                        .weight(1f)
                                                )
                                            }
                                        }
                                    },
                                    modifier = Modifier.padding(
                                        start = if (editCategories) 8.dp else 0.dp,
                                        end = if (editCategories) 0.dp else 8.dp
                                    )
                                )
                            }
                            if (editCategories) {
                                if (!openAddCategoryField) {
                                    AssistChip(
                                        onClick = {
                                            openAddCategoryField = true
                                        },
                                        label = {
                                            Icon(
                                                painter = painterResource(id = R.drawable.baseline_add_24),
                                                contentDescription = "add category",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        },
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )
                                } else {
                                    BasicTextField(
                                        value = categoryInput,
                                        onValueChange = { categoryInput = it },
                                        modifier = Modifier
                                            .focusRequester(focusRequesterCategory),
                                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                                        keyboardActions = KeyboardActions(
                                            onDone = {
                                                if (categoryInput.isNotBlank()) {
                                                    recipeViewModel.addCategory(categoryInput.trim())
                                                }
                                                categoryInput = ""
                                                openAddCategoryField = false
                                                keyboardController?.hide()
                                            }
                                        ),
                                        decorationBox = { innerTextField ->
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                border = BorderStroke(
                                                    1.dp,
                                                    MaterialTheme.colorScheme.outline
                                                ),
                                                tonalElevation = 1.dp,
                                                modifier = Modifier
                                                    .defaultMinSize(minHeight = 32.dp)
                                                    .padding(horizontal = 8.dp, vertical = 8.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(
                                                        start = 16.dp,
                                                        end = 8.dp,
                                                        top = 4.dp,
                                                        bottom = 4.dp
                                                    )
                                                ) {
                                                    Box {
                                                        if (categoryInput.isEmpty()) {
                                                            Text(
                                                                text = stringResource(R.string.category),
                                                                style = MaterialTheme.typography.bodyMedium,
                                                                color = MaterialTheme.colorScheme.outline
                                                            )
                                                        }
                                                        innerTextField()
                                                    }
                                                    Spacer(Modifier.width(4.dp))
                                                    Icon(
                                                        painter = painterResource(R.drawable.baseline_check_24),
                                                        contentDescription = "Add category",
                                                        modifier = Modifier
                                                            .size(24.dp)
                                                            .clickable {
                                                                if (categoryInput.isNotBlank()) {
                                                                    recipeViewModel.addCategory(
                                                                        categoryInput
                                                                    )
                                                                }
                                                                categoryInput = ""
                                                                openAddCategoryField = false
                                                                keyboardController?.hide()
                                                            },
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // === Ingredients Section ===
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showIngredients = !showIngredients }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(R.string.ingredients),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (editMode) {
                                IconButton(
                                    onClick = {
                                        if (!editIngredients) {
                                            editIngredients = true
                                            showIngredients = true
                                        } else {
                                            recipeViewModel.updateRecipe(recipe)
                                            editIngredients = false
                                        }
                                    },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = if (!editIngredients) R.drawable.baseline_edit_24 else R.drawable.baseline_save_24),
                                        contentDescription = "Edit ingredients",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                imageVector = if (showIngredients) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Toggle ingredients",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                            thickness = 1.dp
                        )
                    }

                    // List of items
                    if (showIngredients) {
                        if (!editIngredients) {
                            Column(
                                modifier = Modifier.padding(horizontal = 8.dp),
                            ) {
                                recipe.itemSet.itemList.forEachIndexed { index, item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = item.name,
                                            modifier = Modifier.weight(1f),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        if (item.amount != 0.0) {
                                            Text(
                                                text = buildString {
                                                    append(item.amount.toString().replace(".0", ""))
                                                    if (item.unit.isNotEmpty()) append(" ${item.unit}")
                                                },
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    if (index != recipe.itemSet.itemList.lastIndex) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(horizontal = 16.dp),
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(
                                                alpha = 0.4f
                                            ),
                                            thickness = 1.dp
                                        )
                                    }
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp),
                            ) {
                                recipe.itemSet.itemList.forEach { item ->
                                    key(item.tmpId) {
                                        val focusRequesterIngredients =
                                            remember { FocusRequester() }
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            OutlinedTextField(
                                                value = item.name,
                                                onValueChange = { newName ->
                                                    recipeViewModel.updateItemSetItemName(
                                                        item.tmpId,
                                                        newName
                                                    )
                                                },
                                                placeholder = { Text(stringResource(R.string.item_name)) },
                                                singleLine = true,
                                                modifier = Modifier
                                                    .weight(0.5f)
                                                    .focusRequester(focusRequesterIngredients)
                                                    .padding(end = 4.dp),
                                                keyboardOptions = KeyboardOptions(
                                                    capitalization = KeyboardCapitalization.Sentences,
                                                    keyboardType = KeyboardType.Text,
                                                    imeAction = ImeAction.Done
                                                ),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                                    unfocusedBorderColor = MaterialTheme.colorScheme.primary
                                                )
                                            )

                                            LaunchedEffect(focusIngredientId, item.tmpId) {
                                                if (focusIngredientId == item.tmpId) {
                                                    focusRequesterIngredients.requestFocus()
                                                    keyboardController?.show()
                                                    focusIngredientId = null
                                                }
                                            }

                                            OutlinedTextField(
                                                value = if (item.amount == 0.0) "" else item.amount.toString()
                                                    .replace(".0", ""),
                                                onValueChange = { newValue ->
                                                    recipeViewModel.updateItemSetItemAmount(
                                                        item.tmpId,
                                                        newValue
                                                    )
                                                },
                                                keyboardOptions = KeyboardOptions.Default.copy(
                                                    keyboardType = KeyboardType.Number
                                                ),
                                                singleLine = true,
                                                modifier = Modifier
                                                    .weight(0.225f)
                                                    .padding(horizontal = 4.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                                    unfocusedBorderColor = MaterialTheme.colorScheme.primary
                                                )
                                            )

                                            UnitDropdown(
                                                unit = item.unit,
                                                onUnitSelected = {
                                                    recipeViewModel.updateItemSetItemUnit(
                                                        item.tmpId,
                                                        it
                                                    )
                                                },
                                                withLabel = false,
                                                modifier = Modifier
                                                    .weight(0.175f)
                                                    .padding(horizontal = 4.dp)
                                            )

                                            IconButton(
                                                onClick = {
                                                    recipeViewModel.deleteItemSetItem(item.tmpId)
                                                },
                                                modifier = Modifier.weight(0.1f)
                                            ) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.baseline_delete_24),
                                                    contentDescription = "Remove"
                                                )
                                            }
                                        }
                                    }
                                }

                                Button(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    onClick = {
                                        recipeViewModel.addEmptyItemSetItem(
                                            onSuccess = { newTmpId ->
                                                focusIngredientId = newTmpId
                                            }
                                        )
                                    },
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                                ) {
                                    Text(stringResource(R.string.new_ingredient))
                                }
                            }
                        }
                    }

                    // === Instructions Section ===
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showInstructions = !showInstructions }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(R.string.instructions),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (editMode) {
                                IconButton(
                                    onClick = {
                                        if (!editInstructions) {
                                            editInstructions = true
                                            showInstructions = true
                                        } else {
                                            recipeViewModel.updateRecipe(recipe)
                                            editInstructions = false
                                        }
                                    },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = if (!editInstructions) R.drawable.baseline_edit_24 else R.drawable.baseline_save_24),
                                        contentDescription = "Edit instructions",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                imageVector = if (showInstructions) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Toggle instructions",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                            thickness = 1.dp
                        )
                    }
                    if (showInstructions) {
                        if (!editInstructions) {
                            Column(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .padding(bottom = 16.dp),
                            ) {
                                recipe.instructions.forEachIndexed { index, instruction ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text(
                                            text = "${index + 1}.",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Medium
                                            ),
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                        Text(
                                            text = instruction,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    if (index != recipe.instructions.lastIndex) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(horizontal = 16.dp),
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(
                                                alpha = 0.4f
                                            ),
                                            thickness = 1.dp
                                        )
                                    }
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp),
                            ) {
                                recipe.instructions.forEachIndexed { index, instruction ->
                                    val focusRequesterInstructions = remember { FocusRequester() }
                                    OutlinedTextField(
                                        value = instruction,
                                        onValueChange = { newValue ->
                                            recipeViewModel.updateInstructionAtIndex(
                                                index,
                                                newValue
                                            )
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .focusRequester(focusRequesterInstructions),
                                        label = { Text(stringResource(R.string.step, index + 1)) },
                                        trailingIcon = {
                                            IconButton(
                                                onClick = {
                                                    recipeViewModel.deleteInstructionAtIndex(index)
                                                }
                                            ) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.baseline_delete_24),
                                                    contentDescription = "Delete image",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    )
                                    LaunchedEffect(focusInstructionIndex, index) {
                                        if (focusInstructionIndex == index && instruction.isEmpty()) {
                                            focusRequesterInstructions.requestFocus()
                                            keyboardController?.show()
                                            focusInstructionIndex = null
                                        }
                                    }
                                }
                                Button(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    onClick = {
                                        recipeViewModel.addEmptyInstruction(
                                            onSuccess = { newIndex ->
                                                focusInstructionIndex = newIndex
                                            }
                                        )
                                    },
                                    elevation = ButtonDefaults.buttonElevation(
                                        defaultElevation = 6.dp
                                    ),
                                ) {
                                    Text(stringResource(R.string.new_step))
                                }
                            }
                        }
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 16.dp),
                        ) {
                            recipe.recipeFileIds.forEach { fileId ->
                                RecipeFileItem(
                                    fileId = fileId,
                                    recipeId = recipe.id,
                                    token = token,
                                    imageLoader = imageLoader,
                                    editMode = editInstructions,
                                    onDeleteConfirmed = { recipeFileId ->
                                        recipeViewModel.deleteRecipeFile(recipeFileId as String)
                                    }
                                )
                            }
                            selectedFiles.forEach { file ->
                                RecipeFileItem(
                                    file = file,
                                    editMode = editInstructions,
                                    onDeleteConfirmed = {
                                        recipeViewModel.removeSelectedRecipeFile(file)
                                    }
                                )
                            }
                            if (editInstructions) {
                                Button(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    onClick = {
                                        launcher.launch(arrayOf("image/*"))
                                    },
                                    elevation = ButtonDefaults.buttonElevation(
                                        defaultElevation = 6.dp
                                    ),
                                ) {
                                    Text("Add image")
                                }
                            }
                        }
                    }
                }
                if (showDialog) {
                    BasicAlertDialog(
                        onDismissRequest = { showDialog = false },
                        content = {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .wrapContentSize()
                                    .padding(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.exit_editing_mode_and_save),
                                        style = MaterialTheme.typography.titleLarge,
                                        modifier = Modifier
                                            .padding(bottom = 8.dp)
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                    ) {
                                        Button(
                                            onClick = {
                                                showDialog = false
                                                editMode = false
                                                navController.popBackStack()
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                        ) {
                                            Text(stringResource(R.string.cancel))
                                        }
                                        Button(
                                            onClick = {
                                                recipeViewModel.updateRecipe(recipe)
                                                showDialog = false
                                                editMode = false
                                                navController.popBackStack()
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                        ) {
                                            Text(stringResource(R.string.save))
                                        }
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    )
}

@Composable
fun RecipeFileItem(
    fileId: String? = null,           // remote file ID
    file: File? = null,               // local selected file
    recipeId: String? = null,         // needed only if fileId != null
    token: String? = null,            // needed only if fileId != null
    imageLoader: ImageLoader? = null, // needed only if fileId != null
    editMode: Boolean,
    onDeleteConfirmed: (Any) -> Unit  // returns either String (fileId) or File
) {
    var confirmDelete by remember { mutableStateOf(false) }
    var showPreview by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val model = when {
        file != null -> file
        fileId != null && recipeId != null && token != null && imageLoader != null -> {
            ImageRequest.Builder(context)
                .data("https://shopit-oracle.mooo.com:8443/api/recipe/$recipeId/files/$fileId")
                .addHeader("Authorization", "Bearer $token")
                .build()
        }

        else -> null
    }

    if (model == null) return // nothing to show

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        if (file != null) {
            // Local file
            AsyncImage(
                model = file,
                contentDescription = "Selected recipe image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clickable {
                        if (!editMode) {
                            showPreview = true
                        }
                    },
                contentScale = ContentScale.Fit
            )
        } else if (fileId != null && imageLoader != null) {
            // Remote file
            AsyncImage(
                model = model,
                imageLoader = imageLoader,
                contentDescription = "Recipe image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clickable {
                        if (!editMode) {
                            showPreview = true
                        }
                    },
                contentScale = ContentScale.Fit
            )
        }

        if (editMode) {
            if (!confirmDelete) {
                IconButton(
                    onClick = { confirmDelete = true },
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(36.dp)
                        .background(Color.LightGray.copy(alpha = 0.7f), CircleShape)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_delete_24),
                        contentDescription = "Delete image",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(
                        onClick = {
                            onDeleteConfirmed(file ?: fileId!!)
                            confirmDelete = false
                        },
                        modifier = Modifier.background(
                            color = Color.LightGray.copy(alpha = 0.7f),
                            shape = CircleShape
                        )
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_check_24),
                            contentDescription = "confirm delete",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = { confirmDelete = false },
                        modifier = Modifier.background(
                            color = Color.LightGray.copy(alpha = 0.7f),
                            shape = CircleShape
                        )
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
    if (showPreview && imageLoader != null) {
        Dialog(onDismissRequest = { showPreview = false }) {
            Box(
                modifier = Modifier
                    .wrapContentSize(),
                contentAlignment = Alignment.Center
            ) {
                var scale by remember { mutableStateOf(1f) }
                var offset by remember { mutableStateOf(Offset.Zero) }

                AsyncImage(
                    model = model,
                    imageLoader = imageLoader,
                    contentDescription = "Preview image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(1f, 5f) // limit zoom
                                offset = if (scale > 1f) offset + pan else Offset.Zero
                            }
                        }
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        ),
                    contentScale = ContentScale.Fit
                )
                IconButton(
                    onClick = { showPreview = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(Color.White.copy(alpha = 0.8f), CircleShape)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_close_24),
                        contentDescription = "Close preview",
                        tint = Color.DarkGray
                    )
                }
            }
        }
    }
}

private fun getFileNameFromUri(context: Context, uri: Uri): String? {
    var result: String? = null
    if (uri.scheme == "content") {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    result = cursor.getString(nameIndex)
                }
            }
        }
    }
    if (result == null) {
        result = uri.path?.substringAfterLast('/')
    }
    return result
}
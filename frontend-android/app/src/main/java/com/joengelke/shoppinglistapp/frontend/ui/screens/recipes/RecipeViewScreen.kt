package com.joengelke.shoppinglistapp.frontend.ui.screens.recipes

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import com.joengelke.shoppinglistapp.frontend.models.RecipeSource
import com.joengelke.shoppinglistapp.frontend.models.Visibility
import com.joengelke.shoppinglistapp.frontend.ui.components.AppScaffold
import com.joengelke.shoppinglistapp.frontend.ui.components.AppTopBar
import com.joengelke.shoppinglistapp.frontend.ui.components.VisibilityDropdown
import com.joengelke.shoppinglistapp.frontend.viewmodel.ItemSetsViewModel
import com.joengelke.shoppinglistapp.frontend.viewmodel.RecipeViewModel
import com.joengelke.shoppinglistapp.frontend.viewmodel.ShoppingListViewModel
import com.joengelke.shoppinglistapp.frontend.viewmodel.UserViewModel

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

    var showIconButtons by remember { mutableStateOf(false) }
    var editMode by remember { mutableStateOf(false) }

    var showShoppingLists by remember { mutableStateOf(false) }
    val shoppingLists by shoppingListViewModel.shoppingLists.collectAsState()

    var showDescription by remember { mutableStateOf(true) }

    var showCategories by remember { mutableStateOf(true) }
    var openAddCategoryField by remember { mutableStateOf(false) }
    var categoryInput by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(openAddCategoryField) {
        if (openAddCategoryField) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    var showIngredients by remember { mutableStateOf(true) }
    val units = listOf("", "ml", "l", "g", "kg", "EL", "TL", stringResource(R.string.pieces_short))

    var showInstructions by remember { mutableStateOf(true) }

    var showDialog by remember { mutableStateOf(false) }

    AppScaffold(
        topBar = {
            Column() {
                AppTopBar(
                    title = recipe?.name ?: stringResource(R.string.loading),
                    showNavigationIcon = true,
                    onNavigationClick = {
                        if (editMode) {
                            showDialog = true
                        } else {
                            navController.popBackStack()
                        }
                    },
                    navController = navController,
                    actions = {
                        IconButton(
                            onClick = {
                                showIconButtons = !showIconButtons
                            }
                        ) {
                            Icon(
                                imageVector = if (!showIconButtons) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                                contentDescription = "show all buttons ",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                )
                AnimatedVisibility(visible = showIconButtons) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        recipe?.let { recipe ->
                            if (!editMode) {
                                Box {
                                    IconButton(
                                        onClick = {
                                            showShoppingLists = true
                                        }
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.baseline_library_add_24),
                                            contentDescription = "add recipe to ",
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = showShoppingLists,
                                        onDismissRequest = { showShoppingLists = false }
                                    ) {

                                        Text(
                                            text = stringResource(R.string.add_ingredients_to),
                                            modifier = Modifier
                                                .padding(start = 12.dp, end = 12.dp, top = 0.dp, bottom = 4.dp)
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
                                                            showShoppingLists = false
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
                                                            showShoppingLists = false
                                                        }
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            if (currentUserId == recipe.creatorId) {
                                if (recipe.visibility == Visibility.SHARED) {
                                    IconButton(
                                        onClick = {
                                            //TODO add User to Recipe Screen
                                        }
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.baseline_person_add_24),
                                            contentDescription = "share User with recipe",
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                                if (!editMode) {
                                    VisibilityDropdown(
                                        visibility = recipe.visibility,
                                        recipeId = recipeId
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        if (editMode) {
                                            recipeViewModel.updateRecipe(recipe)
                                            editMode = false
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
                                            painter = painterResource(id = R.drawable.baseline_save_24),
                                            contentDescription = "edit recipe",
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
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
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = if (showDescription) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
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
                        if (!editMode && recipe.description.isNotBlank()) {
                            Text(
                                text = recipe.description,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .padding(bottom = 12.dp)
                            )
                        }
                        if (editMode) {
                            OutlinedTextField(
                                value = recipe.description,
                                onValueChange = { recipeViewModel.updateDescription(it) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
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
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = if (showCategories) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
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
                                    if (editMode) {
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
                                        if (confirmDelete && editMode) {
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
                                        if (editMode) {
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
                                        start = if (editMode) 8.dp else 0.dp,
                                        end = if (editMode) 0.dp else 8.dp
                                    )
                                )
                            }
                            if (editMode) {
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
                                            .focusRequester(focusRequester),
                                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                                        keyboardActions = KeyboardActions(
                                            onDone = {
                                                if (categoryInput.isNotBlank()) {
                                                    recipeViewModel.addCategory(categoryInput.trim())
                                                    categoryInput = ""
                                                    openAddCategoryField = false
                                                    keyboardController?.hide()
                                                }
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
                                                                    categoryInput = ""
                                                                    openAddCategoryField = false
                                                                    keyboardController?.hide()
                                                                }
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
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = if (showIngredients) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
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
                        if (!editMode) {
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
                                        Text(
                                            text = buildString {
                                                if (item.amount != 0.0) {
                                                    append(item.amount.toString().replace(".0", ""))
                                                    append(" ")
                                                }
                                                append(item.unit)
                                            },
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
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
                                    var expandedUnitDropdown by remember { mutableStateOf(false) }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = item.name,
                                            onValueChange = { newName ->
                                                recipeViewModel.updateItemSetItemName(
                                                    item.id,
                                                    newName
                                                )
                                            },
                                            placeholder = { Text(stringResource(R.string.item_name)) },
                                            singleLine = true,
                                            modifier = Modifier.weight(0.55f),
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

                                        OutlinedTextField(
                                            value = if (item.amount == 0.0) "" else item.amount.toString()
                                                .replace(".0", ""),
                                            onValueChange = { newValue ->
                                                recipeViewModel.updateItemSetItemAmount(
                                                    item.id,
                                                    newValue
                                                )
                                            },
                                            keyboardOptions = KeyboardOptions.Default.copy(
                                                keyboardType = KeyboardType.Number
                                            ),
                                            singleLine = true,
                                            modifier = Modifier.weight(0.2f),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                                unfocusedBorderColor = MaterialTheme.colorScheme.primary
                                            )
                                        )

                                        ExposedDropdownMenuBox(
                                            expanded = expandedUnitDropdown,
                                            onExpandedChange = {
                                                expandedUnitDropdown = !expandedUnitDropdown
                                            },
                                            modifier = Modifier.weight(0.2f)
                                        ) {
                                            OutlinedTextField(
                                                value = item.unit.ifEmpty { " " },
                                                onValueChange = {},
                                                readOnly = true,
                                                enabled = false,
                                                singleLine = true,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .menuAnchor(
                                                        MenuAnchorType.PrimaryNotEditable,
                                                        expandedUnitDropdown
                                                    )
                                                    .clickable { expandedUnitDropdown = true },
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    disabledBorderColor = MaterialTheme.colorScheme.primary,
                                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                )
                                            )
                                            ExposedDropdownMenu(
                                                expanded = expandedUnitDropdown,
                                                onDismissRequest = { expandedUnitDropdown = false }
                                            ) {
                                                units.forEach { unit ->
                                                    DropdownMenuItem(
                                                        text = { Text(unit) },
                                                        onClick = {
                                                            recipeViewModel.updateItemSetItemUnit(
                                                                item.id,
                                                                unit
                                                            )
                                                            expandedUnitDropdown = false
                                                        }
                                                    )
                                                }
                                            }
                                        }

                                        IconButton(onClick = {
                                            recipeViewModel.deleteItemSetItem(item.id)
                                        }) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.baseline_delete_24),
                                                contentDescription = "Remove"
                                            )
                                        }
                                    }
                                }

                                Button(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    onClick = {
                                        recipeViewModel.addEmptyItemSetItem()
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
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = if (showInstructions) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
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
                        if (!editMode) {
                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp),
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
                                modifier = Modifier.padding(horizontal = 16.dp),
                            ) {
                                recipe.instructions.forEachIndexed { index, instruction ->
                                    OutlinedTextField(
                                        value = instruction,
                                        onValueChange = { newValue ->
                                            recipeViewModel.updateInstructionAtIndex(
                                                index,
                                                newValue
                                            )
                                        },
                                        label = { Text(stringResource(R.string.step, index + 1)) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    )
                                }
                                Button(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    onClick = {
                                        recipeViewModel.addEmptyInstruction()
                                    },
                                    elevation = ButtonDefaults.buttonElevation(
                                        defaultElevation = 6.dp
                                    ),
                                ) {
                                    Text(stringResource(R.string.new_step))
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
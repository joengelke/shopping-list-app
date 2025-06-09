package com.joengelke.shoppinglistapp.frontend.ui.screens.shoppingitems

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.joengelke.shoppinglistapp.frontend.R
import com.joengelke.shoppinglistapp.frontend.models.ShoppingItem
import com.joengelke.shoppinglistapp.frontend.navigation.Routes
import com.joengelke.shoppinglistapp.frontend.ui.common.ShoppingItemsSortCategory
import com.joengelke.shoppinglistapp.frontend.ui.common.ShoppingItemsSortOptions
import com.joengelke.shoppinglistapp.frontend.ui.common.SortDirection
import com.joengelke.shoppinglistapp.frontend.viewmodel.SettingsViewModel
import com.joengelke.shoppinglistapp.frontend.viewmodel.ShoppingItemsViewModel
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingItemsOverviewScreen(
    navController: NavHostController,
    shoppingListName: String,
    shoppingListId: String,
    settingsViewModel: SettingsViewModel,
    shoppingItemsViewModel: ShoppingItemsViewModel = hiltViewModel()
) {

    val sortedShoppingItems by shoppingItemsViewModel.sortedShoppingItems.collectAsState()
    val isUndoAvailable by shoppingItemsViewModel.isUndoAvailable.collectAsState()
    var isMenuExpanded by remember { mutableStateOf(false) }

    var refreshing by remember { mutableStateOf(false) }
    val state = rememberPullToRefreshState()
    val onRefresh: () -> Unit = {
        refreshing = true
        shoppingItemsViewModel.loadShoppingItems(
            shoppingListId,
            onSuccess = { refreshing = false }
        )
    }

    LaunchedEffect(shoppingListId) {
        onRefresh()
        while (true) {
            shoppingItemsViewModel.loadShoppingItems(shoppingListId, onSuccess = {})
            delay(10_000) // 10 seconds auto refresh
        }
    }
    val uncheckedItems = sortedShoppingItems.filter { !it.checked }
    val checkedItems = sortedShoppingItems.filter { it.checked }

    var isCheckedItemsVisible by remember { mutableStateOf(false) }

    var showSorting by remember { mutableStateOf(false) }
    val shoppingItemsSortOption by settingsViewModel.shoppingItemsSortOption.collectAsState()


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = shoppingListName,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    if (isUndoAvailable) {
                        IconButton(
                            onClick = {
                                shoppingItemsViewModel.undoLastCheckedItem()
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_undo_24),
                                contentDescription = "undo",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            navController.navigate(
                                Routes.ShoppingListUser.createRoute(
                                    shoppingListId
                                )
                            )
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_person_add_24),
                            contentDescription = "add User to List",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    IconButton(
                        onClick = {
                            isMenuExpanded = !isMenuExpanded
                        }
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    DropdownMenu(
                        expanded = isMenuExpanded,
                        onDismissRequest = { isMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.item_sets)) },
                            onClick = {
                                navController.navigate(
                                    Routes.ItemSetOverview.createRoute(
                                        shoppingListId
                                    )
                                )
                                isMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.sorted_by)) },
                            onClick = {
                                showSorting = true
                                isMenuExpanded = false
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(Routes.ShoppingItemsCreate.createRoute(shoppingListId))
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }
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
                // unchecked list
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uncheckedItems) { item ->
                        /*
                        // TODO bug: sometimes 2 container disappear
                        var visible by remember { mutableStateOf(true) }
                        AnimatedVisibility(
                            visible = visible
                        ) {

                         */
                        ShoppingItemContainer(
                            item,
                            onCheckedChange = { updatedItem ->
                                //visible = false
                                shoppingItemsViewModel.updateCheckedStatus(
                                    shoppingListId,
                                    item,
                                    updatedItem.checked
                                )
                            },
                            onEditClick = { updatedItem ->
                                shoppingItemsViewModel.updateItem(
                                    updatedItem
                                )
                            },
                            onDeleteClick = { deleteItem ->
                                shoppingItemsViewModel.deleteItem(
                                    shoppingListId, deleteItem.id
                                )
                            }
                        )
                        /*
                        }

                         */
                    }

                    if (checkedItems.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isCheckedItemsVisible = !isCheckedItemsVisible }
                                    .padding(vertical = 8.dp, horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isCheckedItemsVisible) stringResource(
                                        R.string.hide_items,
                                        checkedItems.size
                                    ) else stringResource(R.string.show_items, checkedItems.size),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = if (isCheckedItemsVisible) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                                    contentDescription = "Toggle Checked Items"
                                )
                            }
                        }

                        if (isCheckedItemsVisible) {
                            items(checkedItems) { item ->
                                ShoppingItemContainer(
                                    item,
                                    onCheckedChange = { updatedItem ->
                                        shoppingItemsViewModel.updateCheckedStatus(
                                            shoppingListId,
                                            item,
                                            updatedItem.checked
                                        )
                                    },
                                    onEditClick = { updatedItem ->
                                        shoppingItemsViewModel.updateItem(
                                            updatedItem
                                        )
                                    },
                                    onDeleteClick = { deletedItem ->
                                        shoppingItemsViewModel.deleteItem(
                                            shoppingListId, deletedItem.id
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    )
    if (showSorting) {
        SortShoppingItemsModal(
            shoppingItemsSortOption,
            onChooseSortOption = { option ->
                settingsViewModel.setShoppingItemsSortOption(option)
            },
            onDismiss = { showSorting = false }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShoppingItemContainer(
    shoppingItem: ShoppingItem,
    onCheckedChange: (ShoppingItem) -> Unit,
    onEditClick: (ShoppingItem) -> Unit,
    onDeleteClick: (ShoppingItem) -> Unit
) {
    var showEditModal by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    fun getUsernameColor(username: String): Color {
        // Generate a hash value based on the username
        val hash = username.hashCode()

        // Generate color from hash
        val red = (hash shr 16) and 0xFF
        val green = (hash shr 8) and 0xFF
        val blue = hash and 0xFF

        // Return a Color using RGB values, ensuring they stay within the valid range
        return Color(red / 255f, green / 255f, blue / 255f)
    }


    fun getTimeAgo(checkedAt: String): String {
        // format editedAt to Instant
        val formatter = DateTimeFormatter.ISO_DATE_TIME
        val editedAtInstant = Instant.from(formatter.parse(checkedAt))

        val now = Instant.now()

        // Calculate the difference in duration between the edited time and current time
        val diff = Duration.between(editedAtInstant, now)

        return when {
            diff.toDays() > 0 -> context.getString(R.string.days_ago, diff.toDays().toString())
            diff.toHours() > 0 -> context.getString(R.string.hours_ago, diff.toHours().toString())
            else -> context.getString(R.string.minutes_ago, diff.toMinutes().toString())
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { showEditModal = true },
                onLongClick = {
                    showDialog = true
                }
            ),
        elevation = CardDefaults.cardElevation(0.dp),
        shape = RectangleShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = shoppingItem.checked,
                onCheckedChange = { isChecked ->
                    onCheckedChange(
                        shoppingItem.copy(
                            checked = isChecked,
                            checkedAt = shoppingItem.checkedAt ?: ""
                        )
                    )
                },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.primary
                )
            )

            // name and note
            Column(
                modifier = Modifier
                    .padding(end = 2.dp)
                    .weight(1f)
            ) {
                Text(
                    text = shoppingItem.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )
                if (shoppingItem.note.isNotBlank()) {
                    Text(
                        text = shoppingItem.note,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }


            // category
            /*
            if (shoppingItem.category.isNotBlank()) {
                   Text(text = shoppingItem.category, fontSize = 12.sp, color = Color.Blue)
               }
             */

            // amount and unit
            if (shoppingItem.amount > 0.0) {
                Text(
                    // checks if amount is whole number then cut of ".0"
                    text = "${
                        shoppingItem.amount.let {
                            if (it % 1 == 0.0) it.toInt().toString() else it.toString()
                        }
                    } ${shoppingItem.unit}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(end = 10.dp)
                )
            }

            // editedBy
            if (shoppingItem.editedBy.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(getUsernameColor(shoppingItem.editedBy), shape = CircleShape)
                        .clickable {
                            Toast.makeText(
                                context,
                                context.getString(
                                    R.string.added_time_ago_toast,
                                    getTimeAgo(shoppingItem.checkedAt)
                                ),
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = shoppingItem.editedBy.take(2).uppercase(),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
    HorizontalDivider(
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
    )

    // Edit Modal
    if (showEditModal) {
        EditShoppingItemModal(
            shoppingItem = shoppingItem,
            onDismiss = { updatedShoppingItem ->
                onEditClick(updatedShoppingItem)
                showEditModal = false
            }
        )
    }

    // Delete/Edit Dialog
    if (showDialog) {
        Dialog(
            onDismissRequest = { showDialog = false }
        ) {
            Card(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            onDeleteClick(shoppingItem)
                            showDialog = false
                        },
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            stringResource(R.string.delete),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    TextButton(
                        onClick = {
                            showEditModal = true
                            showDialog = false
                        },
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            stringResource(R.string.edit),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditShoppingItemModal(
    shoppingItem: ShoppingItem,
    onDismiss: (ShoppingItem) -> Unit
) {
    var name by remember { mutableStateOf(shoppingItem.name) }
    var amount by remember { mutableDoubleStateOf(shoppingItem.amount) }
    var amountText by remember { mutableStateOf(amount.toString().replace(".0", "")) }
    var unit by remember { mutableStateOf(shoppingItem.unit) }
    var category by remember { mutableStateOf(shoppingItem.category) }
    var note by remember { mutableStateOf(shoppingItem.note) }

    var expandedUnitDropdown by remember { mutableStateOf(false) }
    val units = listOf("", "ml", "l", "g", "kg", stringResource(R.string.pieces_short))


    val updatedShoppingItem = ShoppingItem(
        id = shoppingItem.id,
        name = name,
        category = category,
        amount = amount,
        unit = unit,
        checked = shoppingItem.checked,
        checkedAt = shoppingItem.checkedAt,
        note = note,
        editedAt = shoppingItem.editedAt,
        editedBy = shoppingItem.editedBy
    )

    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = { onDismiss(updatedShoppingItem) },
        shape = RectangleShape,
        modifier = Modifier.wrapContentHeight(),
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                    },
                    label = { Text(stringResource(R.string.item_name)) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { newValue ->
                        amountText = newValue.replace(",", ".")

                        amountText.toDoubleOrNull()?.let { validDouble ->
                            amount = validDouble
                        }
                    },
                    label = { Text(stringResource(R.string.amount)) },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(0.3f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )
                ExposedDropdownMenuBox(
                    expanded = expandedUnitDropdown,
                    onExpandedChange = { expandedUnitDropdown = !expandedUnitDropdown },
                    modifier = Modifier
                        .weight(0.2f)
                ) {
                    OutlinedTextField(
                        value = unit.ifEmpty { " " },
                        onValueChange = {},
                        label = { Text(stringResource(R.string.unit)) },
                        readOnly = true,
                        enabled = false,
                        singleLine = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUnitDropdown)
                        },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, expandedUnitDropdown)
                            .clickable {
                                expandedUnitDropdown = true
                            },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = MaterialTheme.colorScheme.primary,
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expandedUnitDropdown,
                        onDismissRequest = { expandedUnitDropdown = false}
                    ) {
                        units.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    unit = item
                                    expandedUnitDropdown = false
                                }
                            )
                        }
                    }
                }
                IconButton(
                    onClick = {
                        if (amount >= 1) {
                            amount -= 1
                            amountText = amount.toString()
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (amount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(
                                alpha = 0.2f
                            )
                        )
                        .padding(horizontal = 8.dp),
                    enabled = amount > 0,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_remove_24),
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }

                IconButton(
                    onClick = {
                        amount += 1
                        amountText = amount.toString()
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(horizontal = 8.dp),
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_add_24),
                        contentDescription = "Add",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                /* Category Field
                OutlinedTextField(
                    value = category,
                    onValueChange = {
                        category = it
                    },
                    label = { Text("Category") },
                    modifier = Modifier.weight(1f)
                ) */
                OutlinedTextField(
                    value = note,
                    onValueChange = {
                        note = it
                    },
                    label = { Text(stringResource(R.string.note)) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortShoppingItemsModal(
    currentSortOption: ShoppingItemsSortOptions,
    onChooseSortOption: (ShoppingItemsSortOptions) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = { onDismiss() },
        shape = RectangleShape,
        modifier = Modifier.wrapContentHeight(),
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.sort_items_by)
                )
            }
            listOf(
                ShoppingItemsSortCategory.ALPHABETICAL,
                ShoppingItemsSortCategory.CHECKED_AT,
                ShoppingItemsSortCategory.EDITED_AT
            ).forEach { category ->
                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )

                val isSelected = currentSortOption.category == category
                val direction = currentSortOption.direction

                ListItem(
                    headlineContent = {
                        Text(
                            text = when (category) {
                                ShoppingItemsSortCategory.ALPHABETICAL -> stringResource(R.string.alphabetical)
                                ShoppingItemsSortCategory.CHECKED_AT -> stringResource(R.string.sorted_added_to_list)
                                ShoppingItemsSortCategory.EDITED_AT -> stringResource(R.string.sorted_edited)
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val newDirection =
                                if (isSelected && direction == SortDirection.ASCENDING) {
                                    SortDirection.DESCENDING
                                } else {
                                    SortDirection.ASCENDING
                                }
                            onChooseSortOption(ShoppingItemsSortOptions(category, newDirection))
                        },

                    trailingContent = {
                        if (isSelected) {
                            Icon(
                                painter = painterResource(id = if (direction == SortDirection.ASCENDING) R.drawable.baseline_arrow_upward_24 else R.drawable.baseline_arrow_downward_24),
                                contentDescription = if (direction == SortDirection.ASCENDING) "Ascending" else "Descending"
                            )
                        }
                    }

                )
            }
        }

    }
}

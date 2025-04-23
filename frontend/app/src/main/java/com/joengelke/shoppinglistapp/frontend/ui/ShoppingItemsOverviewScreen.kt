package com.joengelke.shoppinglistapp.frontend.ui

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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.joengelke.shoppinglistapp.frontend.R
import com.joengelke.shoppinglistapp.frontend.models.ShoppingItem
import com.joengelke.shoppinglistapp.frontend.navigation.Routes
import com.joengelke.shoppinglistapp.frontend.viewmodel.ShoppingItemsViewModel
import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingItemsOverviewScreen(
    navController: NavHostController,
    shoppingListName: String,
    shoppingListId: String,
    shoppingItemsViewModel: ShoppingItemsViewModel = hiltViewModel()
) {

    val shoppingItems by shoppingItemsViewModel.shoppingItems.collectAsState()
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
        shoppingItemsViewModel.loadShoppingItems(shoppingListId, onSuccess = {})
    }

    val uncheckedItems = shoppingItems.filter { !it.checked }
    val checkedItems = shoppingItems.filter { it.checked }

    var isCheckedItemsVisible by remember { mutableStateOf(false) }
    //val (uncheckedItems, checkedItems) = shoppingItems.partition { !it.checked }

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
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(8.dp)
                        )
                        IconButton(onClick = { isMenuExpanded = !isMenuExpanded }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options")
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.DarkGray
                ),
                actions = {
                    DropdownMenu(
                        expanded = isMenuExpanded,
                        onDismissRequest = { isMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Item Sets") },
                            onClick = {
                                navController.navigate(
                                    Routes.ItemSetOverview.createRoute(
                                        shoppingListId
                                    )
                                )
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
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    // val sortedItems = uncheckedItems + checkedItems

                    items(uncheckedItems) { item ->
                        ShoppingItemContainer(
                            item,
                            onCheckedChange = { updatedItem ->
                                shoppingItemsViewModel.updateCheckedStatus(
                                    item.id,
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
                                    text = if (isCheckedItemsVisible) "Hide ${checkedItems.size} Items" else "Show ${checkedItems.size} Items",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
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
                                            item.id,
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


    fun getTimeAgo(editedAt: String): String {
        // format editedAt to Instant
        val formatter = DateTimeFormatter.ISO_DATE_TIME
        val editedAtInstant = Instant.from(formatter.parse(editedAt))

        val now = Instant.now()

        // Calculate the difference in duration between the edited time and current time
        val diff = Duration.between(editedAtInstant, now)

        return when {
            diff.toDays() > 0 -> "${diff.toDays()} days ago"
            diff.toHours() > 0 -> "${diff.toHours()} hours ago"
            else -> "${diff.toMinutes()} minutes ago"
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp)
            .combinedClickable(
                onClick = { showEditModal = true },
                onLongClick = {
                    showDialog = true
                }
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
                onCheckedChange = { isChecked -> onCheckedChange(shoppingItem.copy(checked = isChecked)) }
            )

            // name and note
            Column(modifier = Modifier.weight(1f)) {
                Text(text = shoppingItem.name, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                if (shoppingItem.note.isNotBlank()) {
                    Text(text = shoppingItem.note, fontSize = 16.sp, color = Color.Gray)
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
                    }${shoppingItem.unit}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 10.dp)
                )
            }

            // editedBy
            if (shoppingItem.editedBy.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(getUsernameColor(shoppingItem.editedBy), shape = CircleShape)
                        .padding(4.dp)
                        .clickable {
                            Toast.makeText(
                                context,
                                "Edited ${getTimeAgo(shoppingItem.editedAt)}",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = shoppingItem.editedBy.take(2).uppercase(),
                        fontSize = 14.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

            }
        }
    }

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
                    .padding(16.dp)
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
                        Text("Delete", fontSize = 20.sp, color = Color.Red)
                    }
                    TextButton(
                        onClick = {
                            showEditModal = true
                            showDialog = false
                        },
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text("Edit", fontSize = 20.sp)
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

    var textState by remember { mutableStateOf(TextFieldValue(unit)) }

    val updatedShoppingItem = ShoppingItem(
        id = shoppingItem.id,
        name = name,
        category = category,
        amount = amount,
        unit = unit,
        checked = shoppingItem.checked,
        note = note,
        editedAt = shoppingItem.editedAt,
        editedBy = shoppingItem.editedBy
    )

    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = { onDismiss(updatedShoppingItem) },
        modifier = Modifier.wrapContentHeight(),
        dragHandle = null
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
                IconButton(onClick = { onDismiss(updatedShoppingItem) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                    },
                    label = { Text("Name") },
                    modifier = Modifier.weight(1f)
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
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(0.3f)
                )
                OutlinedTextField(
                    value = textState,
                    onValueChange = { newValue ->
                        textState = newValue
                        unit = newValue.text
                    },
                    label = { Text("Unit") },
                    modifier = Modifier
                        .weight(0.2f)
                        .onFocusChanged {
                            if (it.isFocused) {
                                textState =
                                    textState.copy(selection = TextRange(0, textState.text.length))
                            }
                        }
                )

                IconButton(
                    onClick = {
                        if ((amount - 1) > 0) {
                            amount -= 1
                            amountText = amount.toString()
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.DarkGray)
                        .padding(horizontal = 8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_remove_24),
                        contentDescription = "Remove",
                        tint = Color.White
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
                        .background(Color.DarkGray)
                        .padding(horizontal = 8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_add_24),
                        contentDescription = "Add",
                        tint = Color.White
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
                    label = { Text("Note") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

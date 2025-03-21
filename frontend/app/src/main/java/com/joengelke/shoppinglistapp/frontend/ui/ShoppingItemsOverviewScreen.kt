package com.joengelke.shoppinglistapp.frontend.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.joengelke.shoppinglistapp.frontend.models.ShoppingItem
import com.joengelke.shoppinglistapp.frontend.navigation.Routes
import com.joengelke.shoppinglistapp.frontend.viewmodel.ShoppingItemsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingItemsOverviewScreen(
    navController: NavHostController,
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

    var isCheckedItemsVisible by remember { mutableStateOf(true) }
    //val (uncheckedItems, checkedItems) = shoppingItems.partition { !it.checked }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "TODO name of shoppinglist",
                            fontSize = 24.sp,
                            modifier = Modifier.padding(16.dp)
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
                            text = { Text("TODO") },
                            onClick = {}
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
                        ShoppingItemContainer(item, onCheckedChange = { updatedItem ->
                            shoppingItemsViewModel.updateCheckedStatus(
                                item.id,
                                updatedItem.checked
                            )
                        })
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
                                    imageVector = if (isCheckedItemsVisible) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Toggle Checked Items"
                                )
                            }
                        }

                        if (isCheckedItemsVisible) {
                            items(checkedItems) { item ->
                                ShoppingItemContainer(item, onCheckedChange = { updatedItem ->
                                    shoppingItemsViewModel.updateCheckedStatus(
                                        item.id,
                                        updatedItem.checked
                                    )
                                })
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun ShoppingItemContainer(
    shoppingItem: ShoppingItem,
    onCheckedChange: (ShoppingItem) -> Unit,
    //onEditClick: (ShoppingItem) -> Unit
) {
    var showModal by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp)
            .clickable { showModal = true }
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

            Column(modifier = Modifier.weight(1f)) {
                Text(text = shoppingItem.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                if (shoppingItem.note.isNotBlank()) {
                    Text(text = shoppingItem.note, fontSize = 14.sp, color = Color.Gray)
                }
            }

            Column(horizontalAlignment = Alignment.End) {
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

                if (shoppingItem.category.isNotBlank()) {
                    Text(text = shoppingItem.category, fontSize = 12.sp, color = Color.Blue)
                }
                if (shoppingItem.createdBy.isNotBlank()) {
                    Text(
                        text = shoppingItem.createdBy,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

            }
        }
    }

    if (showModal) {
        ShoppingItemEditModal(
            shoppingItem = shoppingItem,
            onDismiss = {
                showModal = false
                // save Item
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingItemEditModal(
    shoppingItem: ShoppingItem,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(shoppingItem.name) }
    var amount by remember { mutableDoubleStateOf(shoppingItem.amount) }
    var unit by remember { mutableStateOf(shoppingItem.unit) }
    var note by remember { mutableStateOf(shoppingItem.note) }

    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextField(
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
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    value = amount.toString(),
                    onValueChange = { newValue ->
                        amount = newValue.toDouble()
                    },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(0.4f)
                )
                TextField(
                    value = unit,
                    onValueChange = {
                        unit = it
                    },
                    label = { Text("Unit") },
                    modifier = Modifier.weight(0.4f)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    IconButton(onClick = {
                        if (amount > 0) {
                            amount -= 1
                        }
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = "Remove")
                    }
                    IconButton(onClick = {
                        amount += 1
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                }
            }

            // change here
            if (sheetState.hasPartiallyExpandedState) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
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
}

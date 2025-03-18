package com.joengelke.shoppinglistapp.frontend.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
    val coroutineScope = rememberCoroutineScope()
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

    val (uncheckedItems, checkedItems) = shoppingItems.partition { !it.checked }

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
                    navController.currentBackStackEntry?.savedStateHandle?.set(
                        "shoppingListId",
                        shoppingListId
                    )
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
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    val sortedItems = uncheckedItems + checkedItems

                    items(sortedItems) { item ->
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
    )
}

@Composable
fun ShoppingItemContainer(
    shoppingItem: ShoppingItem,
    onCheckedChange: (ShoppingItem) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp)
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
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
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
}

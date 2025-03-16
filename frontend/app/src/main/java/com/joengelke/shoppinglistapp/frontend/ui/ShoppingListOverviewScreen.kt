package com.joengelke.shoppinglistapp.frontend.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
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
import androidx.navigation.NavController
import com.joengelke.shoppinglistapp.frontend.models.ShoppingList
import com.joengelke.shoppinglistapp.frontend.viewmodel.AuthViewModel
import com.joengelke.shoppinglistapp.frontend.viewmodel.ShoppingListViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListOverviewScreen(
    navController: NavController,
    shoppingListViewModel: ShoppingListViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {

    val shoppingLists by shoppingListViewModel.shoppingLists.collectAsState()
    var isMenuExpanded by remember { mutableStateOf(false) }

    //
    var refreshing by remember { mutableStateOf(false) }
    val state = rememberPullToRefreshState()
    val coroutineScope = rememberCoroutineScope()
    val onRefresh: () -> Unit = {
        refreshing = true
        coroutineScope.launch {
            shoppingListViewModel.loadShoppingLists()
            delay(1000)
            refreshing = false
        }

    }

    LaunchedEffect(Unit) {
        shoppingListViewModel.loadShoppingLists()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Shopping Lists",
                            fontSize = 24.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                        IconButton(onClick = { isMenuExpanded = !isMenuExpanded }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options")
                        }
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
                            text = { Text("Logout") },
                            onClick = {
                                authViewModel.logout()
                                navController.navigate("login") {
                                    // Clear back stack
                                    popUpTo("shoppingListOverview") {
                                        inclusive = true
                                    }
                                }
                            })
                    }
                }
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
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(shoppingLists) { shoppingList ->
                        ShoppingListContainer(
                            shoppingList = shoppingList,
                            navController = navController,
                            onEdit = {},
                            onDelete = {},
                        )
                    }

                    // New Shopping List Button
                    item {
                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            onClick = { navController.navigate("shoppingListCreate") }
                        ) {
                            Text("New Shopping List")
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun ShoppingListContainer(
    shoppingList: ShoppingList,
    navController: NavController,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var isMenuExpanded by remember { mutableStateOf(false) }

    //Container for each list
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable {
                // navigate to shopping items overview with shoppingList Id included
                navController.currentBackStackEntry?.savedStateHandle?.set(
                    "shoppingListId",
                    shoppingList.id
                )
                navController.navigate("shoppingItemsOverview") // forward to items overview
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(text = shoppingList.name, fontWeight = FontWeight.Bold)
                Text(text = "Items: ${shoppingList.itemIds.size}")
            }
            Column {
                // Settings button
                IconButton(onClick = { isMenuExpanded = !isMenuExpanded }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More options")
                }
                // Dropdown Menu
                DropdownMenu(
                    expanded = isMenuExpanded,
                    onDismissRequest = { isMenuExpanded = false },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    DropdownMenuItem(text = { Text("Edit") }, onClick = { onEdit() }) //TODO
                    DropdownMenuItem(text = { Text("Delete") }, onClick = { onDelete() }) //TODO
                }
            }

        }

    }
}
package com.joengelke.shoppinglistapp.frontend.ui.screens.shoppinglist

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.joengelke.shoppinglistapp.frontend.R
import com.joengelke.shoppinglistapp.frontend.models.ShoppingList
import com.joengelke.shoppinglistapp.frontend.navigation.Routes
import com.joengelke.shoppinglistapp.frontend.ui.components.ConfirmationDialog
import com.joengelke.shoppinglistapp.frontend.viewmodel.AuthViewModel
import com.joengelke.shoppinglistapp.frontend.viewmodel.ShoppingListViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListOverviewScreen(
    navController: NavHostController,
    shoppingListViewModel: ShoppingListViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    val shoppingLists by shoppingListViewModel.shoppingLists.collectAsState()
    val uncheckedItemsAmount by shoppingListViewModel.uncheckedItemsAmount.collectAsState()

    var refreshing by remember { mutableStateOf(false) }
    val state = rememberPullToRefreshState()
    val coroutineScope = rememberCoroutineScope()
    val onRefresh: () -> Unit = {
        refreshing = true
        coroutineScope.launch {
            shoppingListViewModel.loadShoppingLists(
                onSuccess = { refreshing = false },
                onFailure = {
                    refreshing = false
                }
            )
            shoppingListViewModel.loadUncheckedItemsAmount()
        }
    }

    LaunchedEffect(Unit) {
        onRefresh()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "List Overview",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .weight(1f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    IconButton(
                        onClick = {
                            navController.navigate("settingsOverview")
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_settings_24),
                            contentDescription = "open settings",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    IconButton(
                        onClick = {
                            authViewModel.logout()
                            navController.navigate("login") {
                                // Clear back stack
                                popUpTo("shoppingListOverview") {
                                    inclusive = true
                                }
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_logout_24),
                            contentDescription = "logout",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
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
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    items(shoppingLists) { shoppingList ->
                        ShoppingListContainer(
                            shoppingList = shoppingList,
                            uncheckedItemsAmount = uncheckedItemsAmount[shoppingList.id] ?: 0,
                            navController = navController,
                            onEdit = { updatedShoppingList ->
                                shoppingListViewModel.updateShoppingList(updatedShoppingList)
                            },
                            onAddUser = {
                                navController.navigate(
                                    Routes.ShoppingListUser.createRoute(
                                        shoppingList.id
                                    )
                                )

                            },
                            onDelete = { shoppingListId ->
                                //
                                shoppingListViewModel.deleteShoppingList(shoppingListId)
                            },
                        )
                    }

                    // New Shopping List Button
                    item {
                        Button(
                            modifier = Modifier
                                .fillMaxWidth(),
                            onClick = { navController.navigate("shoppingListCreate") },
                        ) {
                            Text("New Shopping List")
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
fun ShoppingListContainer(
    shoppingList: ShoppingList,
    uncheckedItemsAmount: Int,
    navController: NavHostController,
    onEdit: (ShoppingList) -> Unit,
    onAddUser: () -> Unit,
    onDelete: (String) -> Unit
) {
    var isMenuExpanded by remember { mutableStateOf(false) }
    var showEditModal by remember { mutableStateOf(false) }
    var deleteShoppingList by remember { mutableStateOf(false) }

    //Container for each list
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp)
            .clickable {
                // forward to items overview
                navController.navigate(
                    Routes.ShoppingItemsOverview.createRoute(
                        shoppingList.name,
                        shoppingList.id
                    )
                )
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                Text(
                    text = shoppingList.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Column {
                // Settings button
                IconButton(onClick = { isMenuExpanded = !isMenuExpanded }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "More options"
                    )
                }
                // Dropdown Menu
                DropdownMenu(
                    expanded = isMenuExpanded,
                    onDismissRequest = { isMenuExpanded = false },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    DropdownMenuItem(text = { Text("Edit") }, onClick = {
                        isMenuExpanded = false
                        showEditModal = true
                    })
                    DropdownMenuItem(text = { Text("User Settings") }, onClick = {
                        isMenuExpanded = false
                        onAddUser()
                    })
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            deleteShoppingList = true
                        }
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_shopping_cart_24),
                contentDescription = "items in list",
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "$uncheckedItemsAmount",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            /*
            Icon(
                painter = painterResource(id = R.drawable.baseline_person_24),
                contentDescription = "open settings",
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "${shoppingList.userIds.size}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
             */
        }
    }

    if (showEditModal) {
        EditShoppingListModal(
            shoppingList = shoppingList,
            onSave = { updatedShoppingList ->
                onEdit(updatedShoppingList)
            },
            onDismiss = {
                showEditModal = false
            }
        )
    }

    if (deleteShoppingList) {
        ConfirmationDialog(
            text = "Delete Shopping List?",
            acceptText = "Delete",
            onDismiss = {
                deleteShoppingList = false
                isMenuExpanded = true
            },
            onCancel = { deleteShoppingList = false },
            onAccept = {
                deleteShoppingList = false
                onDelete(shoppingList.id)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditShoppingListModal(
    shoppingList: ShoppingList,
    onSave: (ShoppingList) -> Unit,
    onDismiss: () -> Unit
) {

    val sheetState = rememberModalBottomSheetState()
    var name by remember { mutableStateOf(shoppingList.name) }

    val updatedShoppingList = ShoppingList(
        id = shoppingList.id,
        name = name,
        createdAt = shoppingList.createdAt,
        itemIds = shoppingList.itemIds,
        itemSetIds = shoppingList.itemSetIds,
        userIds = shoppingList.userIds
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.wrapContentHeight()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                TextButton(
                    onClick = {
                        onSave(updatedShoppingList)
                        onDismiss()
                    }
                ) {
                    Text("Save")
                }
            }
        }
    }
}
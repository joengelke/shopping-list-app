package com.joengelke.shoppinglistapp.frontend.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.joengelke.shoppinglistapp.frontend.R
import com.joengelke.shoppinglistapp.frontend.models.ShoppingList
import com.joengelke.shoppinglistapp.frontend.navigation.Routes
import com.joengelke.shoppinglistapp.frontend.viewmodel.ShoppingListViewModel
import com.joengelke.shoppinglistapp.frontend.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsAdminShoppingListsScreen(
    navController: NavHostController,
    userViewModel: UserViewModel = hiltViewModel(),
    shoppingListViewModel: ShoppingListViewModel = hiltViewModel()
) {
    val allShoppingLists by shoppingListViewModel.allShoppingLists.collectAsState()

    var refreshing by remember { mutableStateOf(false) }
    val state = rememberPullToRefreshState()
    val onRefresh: () -> Unit = {
        refreshing = true
        shoppingListViewModel.loadAllShoppingLists(
            onSuccess = { refreshing = false }
        )
    }

    LaunchedEffect(Unit) {
        userViewModel.updateUserRoles(
            isAdmin = { isAdmin ->
                if (!isAdmin) {
                    navController.popBackStack()
                }
            }
        )
        onRefresh()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.admin_shopping_list_settings),
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
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    items(allShoppingLists) { shoppingList ->
                        ShoppingListAdminContainer(
                            shoppingList,
                            onShoppingListItems = { shoppingListId ->
                                navController.navigate(
                                    Routes.SettingsAdminShoppingItems.createRoute(
                                        shoppingListId
                                    )
                                )
                            },
                            onShoppingListUser = { shoppingListId ->
                                navController.navigate(
                                    Routes.ShoppingListUser.createRoute(
                                        shoppingListId
                                    )
                                )
                            },
                            onDeleteShoppingList = { shoppingListId ->
                                shoppingListViewModel.deleteShoppingList(shoppingListId)
                            }
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun ShoppingListAdminContainer(
    shoppingList: ShoppingList,
    onShoppingListItems: (String) -> Unit,
    onShoppingListUser: (String) -> Unit,
    onDeleteShoppingList: (String) -> Unit
) {
    var showConfirmation by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
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
                .padding(8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!showConfirmation) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .weight(0.6f)
                ) {
                    Text(
                        text = shoppingList.name,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(0.2f)
                        .clickable {
                            onShoppingListItems(shoppingList.id)
                        }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_format_list_bulleted_24),
                        contentDescription = "Remove"
                    )
                    Text(
                        text = "${shoppingList.itemIds.size}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier
                            .padding(start = 8.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(0.2f)
                        .clickable {
                            onShoppingListUser(shoppingList.id)
                        }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_person_24),
                        contentDescription = "Remove"
                    )
                    Text(
                        text = "${shoppingList.userIds.size}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(start = 8.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(
                    onClick = {
                        showConfirmation = true
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_delete_24),
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            } else {
                Text(
                    text = stringResource(R.string.are_you_sure),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .weight(1f),
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(
                    onClick = {
                        showConfirmation = false
                        onDeleteShoppingList(shoppingList.id)
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_check_24),
                        contentDescription = "confirm delete"
                    )
                }
                IconButton(
                    onClick = {
                        showConfirmation = false
                    }
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
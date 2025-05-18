package com.joengelke.shoppinglistapp.frontend.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.joengelke.shoppinglistapp.frontend.R
import com.joengelke.shoppinglistapp.frontend.models.ShoppingItem
import com.joengelke.shoppinglistapp.frontend.viewmodel.ShoppingItemsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsAdminShoppingItemsScreen(
    navController: NavHostController,
    shoppingListId: String,
    shoppingItemsViewModel: ShoppingItemsViewModel = hiltViewModel()
) {
    val shoppingItems by shoppingItemsViewModel.shoppingItems.collectAsState()

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
                            text = "Admin Items Settings",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
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
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    items(shoppingItems) { shoppingItem ->
                        ShoppingItemAdminContainer(
                            shoppingItem,
                            onDeleteShoppingItem = { shoppingItemId ->
                                shoppingItemsViewModel.deleteItem(
                                    shoppingListId,
                                    shoppingItemId
                                )
                            }
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun ShoppingItemAdminContainer(
    shoppingItem: ShoppingItem,
    onDeleteShoppingItem: (String) -> Unit
) {
    var showConfirmation by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth(),
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
            if(!showConfirmation) {
                Text(
                    text = shoppingItem.name,
                    modifier = Modifier
                        .weight(1f)

                    )
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
                    text = "Are you sure?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .weight(1f),
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(
                    onClick = {
                        showConfirmation = false
                        onDeleteShoppingItem(shoppingItem.id)
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
    HorizontalDivider(
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
    )
}
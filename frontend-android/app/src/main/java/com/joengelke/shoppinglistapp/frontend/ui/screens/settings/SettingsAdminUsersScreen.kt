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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.joengelke.shoppinglistapp.frontend.R
import com.joengelke.shoppinglistapp.frontend.models.User
import com.joengelke.shoppinglistapp.frontend.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsAdminUsersScreen(
    navController: NavHostController,
    userViewModel: UserViewModel = hiltViewModel(),
) {
    val allUsers by userViewModel.allUsers.collectAsState()
    val currentUserId by userViewModel.currentUserId.collectAsState()
    val sortedAllUsersList = allUsers.sortedWith(
        compareByDescending<User> { it.id == currentUserId }.thenBy { it.username }
    ) // currentUser is always first in list

    var refreshing by remember { mutableStateOf(false) }
    val state = rememberPullToRefreshState()
    val onRefresh: () -> Unit = {
        refreshing = true
        userViewModel.getAllUsers(
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
        userViewModel.updateCurrentUserId()
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
                            text = stringResource(R.string.admin_user_settings),
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
                    items(sortedAllUsersList) { user ->
                        UserAdminContainer(
                            user,
                            currentUserId,
                            onAddRole = { role ->
                                userViewModel.addRoleToUser(user.id, role)
                            },
                            onRemoveRole = { role ->
                                userViewModel.removeRoleFromUser(user.id, role)
                            },
                            onDeleteUser = { userId ->
                                userViewModel.deleteUser(
                                    userId,
                                    onSuccess = {}
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
fun UserAdminContainer(
    user: User,
    currentUserId: String,
    onAddRole: (String) -> Unit,
    onRemoveRole: (String) -> Unit,
    onDeleteUser: (String) -> Unit
) {
    var showConfirmation by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!showConfirmation) {
                Text(
                    text = if (user.id == currentUserId) {
                        stringResource(R.string.user_me, user.username)
                    } else {
                        user.username
                    },
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .weight(1f),
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(
                    onClick = {
                        if (user.roles.contains("ADMIN")) onRemoveRole("ADMIN") else onAddRole("ADMIN")
                    }
                ) {
                    Icon(
                        painter = painterResource(id = if (user.roles.contains("ADMIN")) R.drawable.baseline_manage_accounts_24 else R.drawable.baseline_person_24),
                        contentDescription = "Remove",
                        tint = if (user.roles.contains("ADMIN")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
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
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .weight(1f),
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(
                    onClick = {
                        showConfirmation = false
                        onDeleteUser(user.id)
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
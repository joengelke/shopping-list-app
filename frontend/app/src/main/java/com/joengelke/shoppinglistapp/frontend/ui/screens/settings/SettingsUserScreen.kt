package com.joengelke.shoppinglistapp.frontend.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.joengelke.shoppinglistapp.frontend.R
import com.joengelke.shoppinglistapp.frontend.ui.components.ConfirmationDialog
import com.joengelke.shoppinglistapp.frontend.ui.components.SettingsContainer
import com.joengelke.shoppinglistapp.frontend.viewmodel.AuthViewModel
import com.joengelke.shoppinglistapp.frontend.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsUserScreen(
    navController: NavHostController,
    userViewModel: UserViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val currentUserId by userViewModel.currentUserId.collectAsState()
    val currentUsername by userViewModel.currentUsername.collectAsState()
    var delete by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        userViewModel.updateCurrentUserId()
        userViewModel.updateUserRoles()
        userViewModel.updateUsername()
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
                            text = currentUsername,
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                item {
                    SettingsContainer(
                        text = "Change Username",
                        onClick = {
                            navController.navigate("settingsUserUsername")
                        }
                    )
                }
                item {
                    SettingsContainer(
                        text = "Change Password",
                        onClick = {
                            navController.navigate("settingsUserPassword")
                        }
                    )
                }
                item {
                    SettingsContainer(
                        text = "Logout",
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_logout_24),
                                contentDescription = "logout"
                            )
                        },
                        onClick = {
                            authViewModel.logout()
                            navController.navigate("login") {
                                // Clear back stack
                                popUpTo("shoppingListOverview") {
                                    inclusive = true
                                }
                            }
                        }
                    )
                }
                item {
                    SettingsContainer(
                        text = "Delete Account",
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_delete_24),
                                contentDescription = "logout"
                            )
                        },
                        onClick = {
                            delete = true
                        }
                    )
                }
            }
        }
    )
    if (delete) {
        ConfirmationDialog(
            text = "Delete User?",
            acceptText = "Delete",
            onDismiss = { delete = false },
            onCancel = { delete = false },
            onAccept = {
                delete = false
                userViewModel.deleteUser(
                    currentUserId,
                    onSuccess = {
                        authViewModel.logout()
                        navController.navigate("login") {
                            // Clear back stack
                            popUpTo("shoppingListOverview") {
                                inclusive = true
                            }
                        }
                    }
                )
            }
        )
    }
}

package com.joengelke.shoppinglistapp.frontend.ui.screens.shoppinglist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
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
import com.joengelke.shoppinglistapp.frontend.models.User
import com.joengelke.shoppinglistapp.frontend.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListUserScreen(
    navController: NavHostController,
    shoppingListId: String,
    userViewModel: UserViewModel = hiltViewModel()
) {
    var username by remember { mutableStateOf("") }
    val userList by userViewModel.listUser.collectAsState()
    val currentUserId by userViewModel.currentUserId.collectAsState()
    var isLoadingAddUser by remember { mutableStateOf(false) }
    var userNotFound by remember { mutableStateOf(false) }
    val sortedUserList = userList.sortedWith(
        compareByDescending<User> { it.id == currentUserId }.thenBy { it.username }
    ) // currentUser is always first in list

    LaunchedEffect(Unit) {
        userViewModel.getShoppingListUser(shoppingListId)
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
                            text = "User Settings",
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = 8.dp, top = 8.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add new user:",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            userNotFound = false
                        },
                        placeholder = { Text("Username") },
                        modifier = Modifier
                            .weight(1f),
                        trailingIcon = {
                            when {
                                isLoadingAddUser -> {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .padding(end = 8.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                userNotFound -> {
                                    Icon(
                                        painter = painterResource(id = R.drawable.baseline_error_24),
                                        contentDescription = "Error",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }

                                else -> {
                                    IconButton(
                                        onClick = {
                                            isLoadingAddUser = true
                                            userViewModel.addUserToShoppingList(
                                                shoppingListId,
                                                username,
                                                onSuccess = {
                                                    username = ""
                                                    userNotFound = false
                                                    isLoadingAddUser = false
                                                },
                                                onFailure = {
                                                    userNotFound = true
                                                    isLoadingAddUser = false
                                                }
                                            )
                                        },
                                        modifier = Modifier
                                            .padding(end = 8.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.baseline_person_add_24),
                                            contentDescription = "Add",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }


                        },
                        supportingText = {
                            if (userNotFound) {
                                Text(
                                    text = "Username doesn't exist",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        isError = userNotFound,
                        shape = RoundedCornerShape(20),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "User with access:",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp)
                ) {
                    items(sortedUserList) { user ->
                        UserContainer(
                            user,
                            currentUserId,
                            removeUser = { userId ->
                                userViewModel.removeUserFromShoppingList(shoppingListId, userId)
                            }
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun UserContainer(
    user: User,
    currentUserId: String,
    removeUser: (String) -> Unit
) {
    var showConfirmation by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
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
                        "${user.username} (me)"
                    } else {
                        user.username
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .weight(1f),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (user.id == currentUserId) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_person_24),
                        contentDescription = "Remove",
                        modifier = Modifier.padding(12.dp)
                    )
                } else {
                    IconButton(
                        onClick = {
                            showConfirmation = true
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_person_remove_24),
                            contentDescription = "Remove"
                        )
                    }
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
                        removeUser(user.id)
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

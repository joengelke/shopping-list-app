package com.joengelke.shoppinglistapp.frontend.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.joengelke.shoppinglistapp.frontend.R
import com.joengelke.shoppinglistapp.frontend.models.User
import com.joengelke.shoppinglistapp.frontend.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListUserScreen(
    navController: NavController,
    shoppingListId: String,
    userViewModel: UserViewModel = hiltViewModel()
) {
    var username by remember { mutableStateOf("") }
    val userList by userViewModel.listUser.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        userViewModel.loadListUser(shoppingListId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "User Settings",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier
                                .padding(8.dp)
                                .weight(1f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.DarkGray
                )
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
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add new user:",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        placeholder = { Text("Username") },
                        modifier = Modifier
                            .weight(1f)
                    )
                    IconButton(
                        onClick = {
                            userViewModel.addUserToShoppingList(
                                shoppingListId,
                                username,
                                onFailure = {
                                    Toast.makeText(
                                        context,
                                        "Username not found",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_person_add_24),
                            contentDescription = "Add",
                            tint = Color.Black
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "User with access:",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                    )
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    //TODO mark own user
                    items(userList) { user ->
                        UserContainer(
                            user,
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
    removeUser: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = user.username,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .weight(1f)
            )
            IconButton(
                onClick = {
                    removeUser(user.id)
                }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_person_remove_24),
                    contentDescription = "Remove",
                    tint = Color.Black
                )
            }
        }
    }
}

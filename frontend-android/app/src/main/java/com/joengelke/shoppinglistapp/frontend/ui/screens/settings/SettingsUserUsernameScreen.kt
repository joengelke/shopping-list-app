package com.joengelke.shoppinglistapp.frontend.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.joengelke.shoppinglistapp.frontend.R
import com.joengelke.shoppinglistapp.frontend.viewmodel.AuthViewModel
import com.joengelke.shoppinglistapp.frontend.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsUserUsernameScreen(
    navController: NavHostController,
    userViewModel: UserViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val currentUsername by userViewModel.currentUsername.collectAsState()
    var newUsername by remember { mutableStateOf(currentUsername) }
    val focusRequester = remember { FocusRequester() }
    var saved by remember { mutableStateOf(false) }
    var usernameTaken by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        userViewModel.updateUsername()
        focusRequester.requestFocus()
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
                            text = stringResource(R.string.change_username),
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .imePadding() // ensures padding above the keyboard
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {

                    OutlinedTextField(
                        value = newUsername,
                        onValueChange = {
                            newUsername = it
                            saved = false
                            usernameTaken = false
                        },
                        label = { Text(stringResource(R.string.new_username)) },
                        supportingText = {
                            if (usernameTaken) {
                                Text(
                                    text = stringResource(R.string.username_already_taken),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        trailingIcon = {
                            if(usernameTaken) {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_error_24),
                                    contentDescription = "Error",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        isError = usernameTaken,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.weight(1f)) // Push content to the top
                    Button(
                        onClick = {
                            saved = true
                            userViewModel.changeUsername(
                                newUsername = newUsername.trim(),
                                usernameTaken = {
                                    usernameTaken = true
                                },
                                onSuccess = {
                                    Toast.makeText(context,
                                        context.getString(R.string.username_changed_please_login_again), Toast.LENGTH_SHORT).show()
                                    authViewModel.logout()
                                    navController.navigate("login") {
                                        // Clear back stack
                                        popUpTo("shoppingListOverview") {
                                            inclusive = true
                                        }
                                    }
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth(),
                        enabled = !saved && newUsername != "",
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(stringResource(R.string.save_and_logout))
                    }
                }
            }
        }
    )
}
package com.joengelke.shoppinglistapp.frontend.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.joengelke.shoppinglistapp.frontend.R
import com.joengelke.shoppinglistapp.frontend.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsUserPasswordScreen(
    navController: NavHostController,
    userViewModel: UserViewModel = hiltViewModel()
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }

    var currentAndNewPasswordMatch by remember { mutableStateOf(false) }
    val newPasswordsMatch =
        newPassword == "" || confirmNewPassword == "" || newPassword == confirmNewPassword
    var confirmed by remember { mutableStateOf(true) }
    var incorrectCurrentPassword by remember { mutableStateOf(false) }

    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmNewPasswordVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current

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
                            text = "Change Password",
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
                        value = currentPassword,
                        onValueChange = {
                            currentPassword = it
                            confirmed = false
                            incorrectCurrentPassword = false
                        },
                        label = { Text("Current Password") },
                        visualTransformation = if (currentPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        supportingText = {
                            if (incorrectCurrentPassword) {
                                Text(
                                    text = "Wrong Password",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        trailingIcon = {
                            if (currentPassword != "") {
                                IconButton(onClick = {
                                    currentPasswordVisible = !currentPasswordVisible
                                }) {
                                    Icon(
                                        painter = painterResource(
                                            id = if (currentPasswordVisible)
                                                R.drawable.baseline_visibility_off_24
                                            else
                                                R.drawable.baseline_visibility_24
                                        ),
                                        contentDescription = if (currentPasswordVisible) "Hide password" else "Show password"
                                    )
                                }
                            }
                        },
                        isError = incorrectCurrentPassword || currentAndNewPasswordMatch,
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = {
                            newPassword = it
                            confirmed = false
                            currentAndNewPasswordMatch = false
                        },
                        label = { Text("New Password") },
                        visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        supportingText = {
                            if (!newPasswordsMatch) {
                                Text(
                                    text = "Password doesn't match",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            if (currentAndNewPasswordMatch) {
                                Text(
                                    text = "Current and new password must be different",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },

                        trailingIcon = {
                            if (newPassword != "") {
                                IconButton(onClick = {
                                    newPasswordVisible = !newPasswordVisible
                                }) {
                                    Icon(
                                        painter = painterResource(
                                            id = if (newPasswordVisible)
                                                R.drawable.baseline_visibility_off_24
                                            else
                                                R.drawable.baseline_visibility_24
                                        ),
                                        contentDescription = if (newPasswordVisible) "Hide password" else "Show password"
                                    )
                                }
                            }
                        },
                        isError = !newPasswordsMatch || currentAndNewPasswordMatch,
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    OutlinedTextField(
                        value = confirmNewPassword,
                        onValueChange = {
                            confirmNewPassword = it
                            confirmed = false
                            currentAndNewPasswordMatch = false
                        },
                        label = { Text("Confirm New Password") },
                        visualTransformation = if (confirmNewPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        supportingText = {
                            if (!newPasswordsMatch) {
                                Text(
                                    text = "Password doesn't match",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            if (currentAndNewPasswordMatch) {
                                Text(
                                    text = "Current and new password must be different",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        trailingIcon = {
                            if (confirmNewPassword != "") {
                                IconButton(onClick = {
                                    confirmNewPasswordVisible = !confirmNewPasswordVisible
                                }) {
                                    Icon(
                                        painter = painterResource(
                                            id = if (confirmNewPasswordVisible)
                                                R.drawable.baseline_visibility_off_24
                                            else
                                                R.drawable.baseline_visibility_24
                                        ),
                                        contentDescription = if (confirmNewPasswordVisible) "Hide password" else "Show password"
                                    )
                                }
                            }
                        },
                        isError = !newPasswordsMatch || currentAndNewPasswordMatch,
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.weight(1f)) // Push content to the top

                    Button(
                        onClick = {
                            confirmed = true
                            userViewModel.changePassword(
                                currentPassword = currentPassword,
                                newPassword = newPassword,
                                samePassword = { currentAndNewPasswordMatch = true },
                                incorrectCurrentPassword = { incorrectCurrentPassword = true },
                                onSuccess = {
                                    Toast.makeText(context, "Password changed!", Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth(),
                        enabled = !confirmed && newPasswordsMatch && currentPassword != "" && newPassword != "" && confirmNewPassword != "",
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Save and back")
                    }
                }
            }
        }
    )
}
package com.joengelke.shoppinglistapp.frontend.ui.screens.auth

import android.widget.Toast
import androidx.compose.animation.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.joengelke.shoppinglistapp.frontend.R
import com.joengelke.shoppinglistapp.frontend.viewmodel.AuthViewModel
import com.joengelke.shoppinglistapp.frontend.viewmodel.SettingsViewModel


@Composable
fun RegisterScreen(
    navController: NavHostController,
    viewModel: AuthViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    // var confirmPassword by remember { mutableStateOf("") } // TODO
    var loading by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    var showServerDropdown by remember { mutableStateOf(false) }
    val currentServerUrl by settingsViewModel.serverUrl.collectAsState()

    var shouldBlink by remember { mutableStateOf(false) }

    val defaultColor = MaterialTheme.colorScheme.primaryContainer
    val blinkColor = MaterialTheme.colorScheme.errorContainer // or any blink color

    val backgroundColor = remember { Animatable(defaultColor) }

    LaunchedEffect(shouldBlink) {
        if (shouldBlink) {
            repeat(3) {
                backgroundColor.animateTo(
                    targetValue = blinkColor,
                    animationSpec = tween(durationMillis = 250)
                )
                backgroundColor.animateTo(
                    targetValue = defaultColor,
                    animationSpec = tween(durationMillis = 250)
                )
            }
            shouldBlink = false
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.register),
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Username TextField
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text(text = stringResource(R.string.username)) },
                enabled = !loading
            )

            // Password TextField
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(R.string.password)) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    if (password != "") {
                        IconButton(onClick = {
                            passwordVisible = !passwordVisible
                        }) {
                            Icon(
                                painter = painterResource(
                                    id = if (passwordVisible)
                                        R.drawable.baseline_visibility_off_24
                                    else
                                        R.drawable.baseline_visibility_24
                                ),
                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                enabled = !loading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Register button
            if (!loading) {
                Button(
                    onClick = {
                        // Perform registration logic
                        loading = true
                        viewModel.register(
                            username, password,
                            onSuccess = { message ->
                                loading = false
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            },
                            onError = { error ->
                                loading = false
                                errorMessage = error
                            })
                    }
                ) {
                    Text(text = stringResource(R.string.register))
                }
            } else {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(36.dp)
                        .align(Alignment.CenterHorizontally),
                    strokeWidth = 3.dp
                )
            }

            errorMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (!loading) {
                TextButton(onClick = { navController.popBackStack() }) {
                    Text(text = stringResource(R.string.already_have_an_account))
                }
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(32.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    showServerDropdown = !showServerDropdown
                },
                containerColor = backgroundColor.value
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_dns_24),
                    contentDescription = "Select server"
                )
            }

            DropdownMenu(
                expanded = showServerDropdown,
                onDismissRequest = { showServerDropdown = false }
            ) {
                listOf(
                    //"https://192.168.1.38:8443/api/" to "DEV Server",
                    "https://192.168.1.60:8443/api/" to "Public Server (From Home Network)",
                    "https://shopit.mooo.com:8443/api/" to "Public Server (DNS Server)"
                ).forEach { (url, label) ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(label)
                                if (currentServerUrl == url) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        },
                        onClick = {
                            settingsViewModel.setServerUrl(url)
                            showServerDropdown = false
                        },
                        enabled = true
                    )
                }
            }
        }
    }
}
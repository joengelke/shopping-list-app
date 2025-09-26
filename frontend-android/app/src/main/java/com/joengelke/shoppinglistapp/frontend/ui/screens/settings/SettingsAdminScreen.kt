package com.joengelke.shoppinglistapp.frontend.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.joengelke.shoppinglistapp.frontend.R
import com.joengelke.shoppinglistapp.frontend.ui.components.SettingsContainer
import com.joengelke.shoppinglistapp.frontend.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsAdminScreen(
    navController: NavHostController,
    userViewModel: UserViewModel = hiltViewModel(),
) {

    LaunchedEffect(Unit) {
        userViewModel.updateUserRoles(
            isAdmin = { isAdmin ->
                if (!isAdmin) {
                    navController.popBackStack()
                }
            }
        )
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
                            text = stringResource(R.string.admin_settings),
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                item {
                    SettingsContainer(
                        text = stringResource(R.string.admin_manage_users),
                        onClick = {
                            navController.navigate("settingsAdminUsers")
                        }
                    )
                }
                item {
                    SettingsContainer(
                        text = stringResource(R.string.admin_manage_shopping_lists),
                        onClick = {
                            navController.navigate("settingsAdminShoppingLists")
                        }
                    )
                }
                item {
                    SettingsContainer(
                        text = stringResource(R.string.admin_manage_recipes),
                        onClick = {
                            navController.navigate("settingsAdminRecipes")
                        }
                    )
                }
                item {
                    SettingsContainer(
                        text = "All item sets (TODO OR DELETE)",
                        onClick = {
                            //TODO
                        }
                    )
                }
            }
        }
    )
}


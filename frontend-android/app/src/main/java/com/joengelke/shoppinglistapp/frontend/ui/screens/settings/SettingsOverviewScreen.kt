package com.joengelke.shoppinglistapp.frontend.ui.screens.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.joengelke.shoppinglistapp.frontend.R
import com.joengelke.shoppinglistapp.frontend.viewmodel.SettingsViewModel
import com.joengelke.shoppinglistapp.frontend.viewmodel.UserViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsOverviewScreen(
    navController: NavHostController,
    settingsViewModel: SettingsViewModel,
    userViewModel: UserViewModel = hiltViewModel(),
) {
    val currentUserId by userViewModel.currentUserId.collectAsState()
    val currentRoles by userViewModel.currentRoles.collectAsState()
    val currentUsername by userViewModel.currentUsername.collectAsState()

    val isAdmin = "ROLE_ADMIN" in currentRoles
    val isUser = "ROLE_USER" in currentRoles

    // Settings:
    val darkMode by settingsViewModel.darkMode.collectAsState()
    val language by settingsViewModel.language.collectAsState()
    val fontScale by settingsViewModel.fontScale.collectAsState()

    val fontScales = listOf(0.8f, 0.9f, 1.0f, 1.1f, 1.2f)
    val currentFontScaleIndex = fontScales.indexOfFirst { it == fontScale }.coerceAtLeast(0)


    val context = LocalContext.current

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
                            text = stringResource(R.string.settings),
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
                    ListItem(
                        headlineContent = {
                            Text(
                                text = if (isUser) if (isAdmin) "$currentUsername [ADMIN]" else "$currentUsername [USER]" else currentUsername,
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        },
                        leadingContent = {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_person_24),
                                contentDescription = "Person Icon"
                            )
                        },
                        trailingContent = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Person Icon"
                            )
                        },
                        modifier = Modifier
                            .clickable {
                                navController.navigate("SettingsUser")
                            }
                    )
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                }
                item {
                    ListItem(
                        headlineContent = {
                            Text(
                                text = stringResource(R.string.general),
                                style = MaterialTheme.typography.headlineMedium
                            )
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        )
                    )
                }
                item {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.dark_mode)) },
                        leadingContent = {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_contrast_24),
                                contentDescription = "dark mode"
                            )
                        },
                        trailingContent = {
                            Switch(
                                checked = darkMode,
                                onCheckedChange = { settingsViewModel.toggleDarkMode() },
                            )
                        }
                    )
                }
                item {
                    ListItem(
                        headlineContent = { Text(text = stringResource(R.string.language)) },
                        leadingContent = {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_language_24),
                                contentDescription = "dark mode"
                            )
                        },
                        modifier = Modifier
                            .clickable {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    val intent = Intent(Settings.ACTION_APP_LOCALE_SETTINGS).apply {
                                        data = Uri.fromParts("package", context.packageName, null)
                                    }
                                    context.startActivity(intent)
                                } else {
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.go_to_settings_system_app_languages_shopit),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                    )
                }
                item {
                    ListItem(
                        headlineContent = {
                            Slider(
                                value = currentFontScaleIndex.toFloat(),
                                onValueChange = { index ->
                                    settingsViewModel.setFontScale(fontScales[index.roundToInt()])
                                },
                                steps = fontScales.size - 2,
                                valueRange = 0f..(fontScales.size - 1).toFloat(),
                            )
                        },
                        leadingContent = {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_text_decrease_24),
                                contentDescription = "Font size decrease",
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingContent = {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_text_increase_24),
                                contentDescription = "Font size increase",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    )
                }
                if (isAdmin) {
                    item {
                        ListItem(
                            headlineContent = { Text(stringResource(R.string.admin_settings)) },
                            supportingContent = {
                            },
                            leadingContent = {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_manage_accounts_24),
                                    contentDescription = "Volume Icon"
                                )
                            },
                            modifier = Modifier
                                .clickable {
                                    navController.navigate("settingsAdmin")
                                }
                        )
                    }
                }
                item {
                    val versionName =
                        context.packageManager
                            .getPackageInfo(context.packageName, 0).versionName

                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Version\n$versionName",
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun LanguageDropdown(
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val options = listOf("en", "de")
    var expanded by remember { mutableStateOf(false) }

    Box {
        TextButton(onClick = { expanded = true }) {
            Text(selectedLanguage.uppercase())
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Select language"
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { lang ->
                DropdownMenuItem(
                    text = { Text(lang.uppercase()) },
                    onClick = {
                        onLanguageSelected(lang)
                        expanded = false
                    }
                )
            }
        }
    }
}


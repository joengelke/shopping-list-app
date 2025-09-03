package com.joengelke.shoppinglistapp.frontend

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.joengelke.shoppinglistapp.frontend.navigation.Navigation
import com.joengelke.shoppinglistapp.frontend.network.RetrofitProvider
import com.joengelke.shoppinglistapp.frontend.ui.common.GlobalEventHandler
import com.joengelke.shoppinglistapp.frontend.ui.theme.AppTheme
import com.joengelke.shoppinglistapp.frontend.viewmodel.AuthViewModel
import com.joengelke.shoppinglistapp.frontend.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var retrofitProvider: RetrofitProvider

    // Launcher for notification permission
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        askNotificationPermission()

        setContent {
            var isInitialized by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                retrofitProvider.initialize()
                isInitialized = true
            }

            if (isInitialized) {
                val authViewModel: AuthViewModel = hiltViewModel()
                val settingsViewModel: SettingsViewModel = hiltViewModel()
                ShoppingListApp(authViewModel, settingsViewModel)
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

@Composable
fun ShoppingListApp(authViewModel: AuthViewModel, settingsViewModel: SettingsViewModel) {

    val navController = rememberNavController()
    val context = LocalContext.current

    val darkMode by settingsViewModel.darkMode.collectAsState()
    val fontScale by settingsViewModel.fontScale.collectAsState()

    AppTheme(
        darkTheme = darkMode,
        fontScale = fontScale
    ) {
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            GlobalEventHandler(
                logoutEvent = authViewModel.logoutEvent,
                disconnectedEvent = authViewModel.disconnectedEvent,
                navController = navController,
                context = context
            )
            Navigation(authViewModel, settingsViewModel, navController)
        }
    }
}
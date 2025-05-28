package com.joengelke.shoppinglistapp.frontend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.joengelke.shoppinglistapp.frontend.navigation.Navigation
import com.joengelke.shoppinglistapp.frontend.ui.common.GlobalEventHandler
import com.joengelke.shoppinglistapp.frontend.ui.theme.AppTheme
import com.joengelke.shoppinglistapp.frontend.viewmodel.AuthViewModel
import com.joengelke.shoppinglistapp.frontend.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val authViewModel: AuthViewModel = hiltViewModel()
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            ShoppingListApp(authViewModel, settingsViewModel)
        }
    }
}

@Composable
fun ShoppingListApp(authViewModel: AuthViewModel, settingsViewModel: SettingsViewModel) {

    val navController = rememberNavController()
    val sessionManager = authViewModel.sessionManager
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
                sessionManager = sessionManager,
                navController = navController,
                context = context
            )
            Navigation(authViewModel, settingsViewModel, navController)
        }
    }
}
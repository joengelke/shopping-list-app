package com.joengelke.shoppinglistapp.frontend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.joengelke.shoppinglistapp.frontend.navigation.Navigation
import com.joengelke.shoppinglistapp.frontend.ui.common.GlobalEventHandler
import com.joengelke.shoppinglistapp.frontend.ui.theme.AppTheme
import com.joengelke.shoppinglistapp.frontend.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val authViewModel: AuthViewModel = hiltViewModel()
            ShoppingListApp(authViewModel)
        }
    }
}

@Composable
fun ShoppingListApp(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val sessionManager = authViewModel.sessionManager
    val context = LocalContext.current
    AppTheme(darkTheme = false) {
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            GlobalEventHandler(
                sessionManager = sessionManager,
                navController = navController,
                context = context
            )
            Navigation(authViewModel, navController)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewShoppingListApp() {
    val authViewModel: AuthViewModel = hiltViewModel()
    ShoppingListApp(authViewModel)
}
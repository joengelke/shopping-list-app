package com.joengelke.shoppinglistapp.frontend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.joengelke.shoppinglistapp.frontend.navigation.Navigation
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
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            Navigation(authViewModel)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewShoppingListApp() {
    val authViewModel: AuthViewModel = hiltViewModel()
    ShoppingListApp(authViewModel)
}
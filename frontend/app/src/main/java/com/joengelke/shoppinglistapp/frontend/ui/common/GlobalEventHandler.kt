package com.joengelke.shoppinglistapp.frontend.ui.common

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import com.joengelke.shoppinglistapp.frontend.network.SessionManager
import kotlinx.coroutines.launch

@Composable
fun GlobalEventHandler(
    sessionManager: SessionManager,
    navController: NavController,
    context: Context
) {
    LaunchedEffect(Unit) {
        launch {
            sessionManager.logoutEvent.collect { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            }
        }

        launch {
            sessionManager.disconnectedEvent.collect { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
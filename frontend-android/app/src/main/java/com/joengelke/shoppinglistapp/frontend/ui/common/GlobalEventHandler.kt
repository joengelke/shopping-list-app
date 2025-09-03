package com.joengelke.shoppinglistapp.frontend.ui.common

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.navigation.NavHostController
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

@Composable
fun GlobalEventHandler(
    logoutEvent: SharedFlow<String>,
    disconnectedEvent: SharedFlow<String>,
    navController: NavHostController,
    context: Context
) {

    LaunchedEffect(Unit) {
        launch {
            logoutEvent.collect { message ->
                //Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            }
        }

        launch {
            disconnectedEvent.collect { message ->
                if (ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
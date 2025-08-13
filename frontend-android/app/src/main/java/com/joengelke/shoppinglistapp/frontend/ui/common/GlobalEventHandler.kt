package com.joengelke.shoppinglistapp.frontend.ui.common

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.navigation.NavHostController
import com.joengelke.shoppinglistapp.frontend.network.SessionManager
import kotlinx.coroutines.launch

@Composable
fun GlobalEventHandler(
    sessionManager: SessionManager,
    navController: NavHostController,
    context: Context
) {

    // val snackbarHostState = remember { SnackbarHostState() }

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
                /* TODO: later if just one global scaffold exists
                // Only show snackbar if app is in the foreground
                if (ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                    snackbarHostState.showSnackbar(message)
                }

                 */
                if (ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
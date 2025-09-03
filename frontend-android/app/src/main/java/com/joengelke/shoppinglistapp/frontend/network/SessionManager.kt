package com.joengelke.shoppinglistapp.frontend.network

import android.content.Context
import com.joengelke.shoppinglistapp.frontend.datastore.LoginDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tokenManager: TokenManager
) {
    private val _logoutEvent = MutableSharedFlow<String>(replay = 0)
    val logoutEvent: SharedFlow<String> = _logoutEvent.asSharedFlow()

    private val _disconnectedEvent = MutableSharedFlow<String>(replay=0)
    val disconnectedEvent: SharedFlow<String> = _disconnectedEvent.asSharedFlow()

    suspend fun logout(message: String) {
        tokenManager.deleteToken()
        _logoutEvent.emit(message) // Emits a one-time event to observers
    }

    suspend fun disconnected(message:String) {
        _disconnectedEvent.emit(message)
    }

    suspend fun isLoggedIn(): Boolean {
        val token = tokenManager.getToken() ?: return false
        return tokenManager.validateToken(token)
    }

    suspend fun refreshLoginState() {
        val token = tokenManager.getToken()
        val isValid = token?.let { tokenManager.validateToken(it) } ?: false
        LoginDataStore.setLoggedIn(context, isValid)
    }
}
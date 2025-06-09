package com.joengelke.shoppinglistapp.frontend.network

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    private val tokenManager: TokenManager
) {
    private val _logoutEvent = MutableSharedFlow<String>(replay = 0)
    val logoutEvent: SharedFlow<String> = _logoutEvent.asSharedFlow()

    private val _disconnectedEvent = MutableSharedFlow<String>(replay=0)
    val disconnectedEvent: SharedFlow<String> = _disconnectedEvent.asSharedFlow()

    // triggers logout event with custom message
    suspend fun logout(message: String) {
        tokenManager.deleteToken()
        _logoutEvent.emit(message) // Emits a one-time event to observers
    }

    suspend fun disconnected(message:String) {
        _disconnectedEvent.emit(message)
    }
}
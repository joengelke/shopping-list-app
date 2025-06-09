package com.joengelke.shoppinglistapp.frontend.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joengelke.shoppinglistapp.frontend.network.SessionManager
import com.joengelke.shoppinglistapp.frontend.network.TokenManager
import com.joengelke.shoppinglistapp.frontend.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// manages authentication state and checks if a token exists and updates UI accordingly
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    val sessionManager: SessionManager,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    fun checkIfTokenIsValid() {
        viewModelScope.launch {
            val token = tokenManager.getToken()
            _isLoggedIn.value = token != null && tokenManager.validateToken(token)
        }
    }

    // Perform login
    fun login(
        username: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = authRepository.login(username, password)
            result.onSuccess {
                _isLoggedIn.value = true
                onSuccess()
            }
            result.onFailure {
                onError("Invalid credentials")
            }
        }
    }

    // Perform register
    fun register(
        username: String,
        password: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val registrationResponse = authRepository.register(username, password)
            registrationResponse.fold(
                onSuccess = { message ->
                    onSuccess(message)
                },
                onFailure = { error ->
                    Log.e(error.message, "Error during registration")
                    onError(error.message ?: "Unknown error")
                }
            )
        }
    }


    // Perform logout
    fun logout() {
        viewModelScope.launch {
            tokenManager.deleteToken()
            _isLoggedIn.value = false
        }
    }
}
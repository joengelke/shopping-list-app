package com.joengelke.shoppinglistapp.frontend.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joengelke.shoppinglistapp.frontend.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// manages authentication state and checks if a token exists and updates UI accordingly
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    fun checkIfTokenIsValid() {
        viewModelScope.launch {
            val token = authRepository.getToken()
            _isLoggedIn.value = token != null && authRepository.validateToken(token)
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
            val token = authRepository.login(username, password)
            if (token != null) {
                _isLoggedIn.value = true
                onSuccess()
            } else {
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
            authRepository.logout()
            _isLoggedIn.value = false
        }
    }
}
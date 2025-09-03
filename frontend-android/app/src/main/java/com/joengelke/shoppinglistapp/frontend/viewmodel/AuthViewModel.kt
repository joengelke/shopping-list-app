package com.joengelke.shoppinglistapp.frontend.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joengelke.shoppinglistapp.frontend.datastore.LoginDataStore
import com.joengelke.shoppinglistapp.frontend.network.SessionManager
import com.joengelke.shoppinglistapp.frontend.repository.AuthRepository
import com.joengelke.shoppinglistapp.frontend.ui.common.LoginState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// manages authentication state and checks if a token exists and updates UI accordingly
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val logoutEvent: SharedFlow<String> = sessionManager.logoutEvent
    val disconnectedEvent: SharedFlow<String> = sessionManager.disconnectedEvent

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Unknown)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()
    //val isLoggedIn: StateFlow<Boolean> = LoginDataStore.isLoggedInFlow(context).stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _credentials = MutableStateFlow<Pair<String, String>?>(null)
    val credentials: StateFlow<Pair<String, String>?> = _credentials.asStateFlow()

    private val _saveCredentials = MutableStateFlow(false)
    val saveCredentials: StateFlow<Boolean> = _saveCredentials.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.credentialsFlow.collect { credentials ->
                _credentials.value = credentials
            }
        }
        viewModelScope.launch {
            authRepository.saveCredentialsFlow.collect { _saveCredentials.value = it }
        }
    }


    fun checkIfTokenIsValid() {
        viewModelScope.launch {
            sessionManager.refreshLoginState()
            val loggedIn = LoginDataStore.isLoggedInFlow(context).first()
            _loginState.value = if (loggedIn) LoginState.LoggedIn else LoginState.LoggedOut
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
                _loginState.value = if (sessionManager.isLoggedIn()) LoginState.LoggedIn else LoginState.LoggedOut
                onSuccess()
            }
            result.onFailure {
                onError(it.message ?: "Unknown error")
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
                    onError(error.message ?: "Unknown error")
                }
            )
        }
    }

    // Perform logout
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
                .onSuccess {
                    _loginState.value = if (sessionManager.isLoggedIn()) LoginState.LoggedIn else LoginState.LoggedOut
                }
                .onFailure {}
        }
    }

    fun setSaveCredentials(enabled: Boolean) {
        viewModelScope.launch {
            authRepository.setSaveCredentials(enabled)
        }
    }
}
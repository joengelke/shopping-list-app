package com.joengelke.shoppinglistapp.frontend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joengelke.shoppinglistapp.frontend.models.User
import com.joengelke.shoppinglistapp.frontend.network.TokenManager
import com.joengelke.shoppinglistapp.frontend.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val tokenManager: TokenManager
): ViewModel() {

    // all users in all shoppingLists
    private val _user = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _user.asStateFlow()

    // users with access to specific shoppingList
    private val _listUser = MutableStateFlow<List<User>>(emptyList())
    val listUser: StateFlow<List<User>> = _listUser.asStateFlow()

    // current logged in user
    private val _currentUserId = MutableStateFlow<String>("")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    fun updateCurrentUserId() {
        viewModelScope.launch {
            val token = tokenManager.getToken()
            // Safely try to get the user ID, or set it to an empty string if the token is invalid or missing
            try {
                _currentUserId.value = token?.let { tokenManager.getUserIdFromToken(it) } ?: ""
            } catch (e: IllegalArgumentException) {
                // Handle the case where the token is invalid or the user ID is missing
                _currentUserId.value = ""
            }
        }
    }

    fun loadAllUser() {

    }

    fun loadListUser(
        shoppingListId: String
    ) {
        viewModelScope.launch {
            val result = userRepository.getShoppingListUser(shoppingListId)
            result.onSuccess { users ->
                _listUser.value = users
            }
        }
    }

    fun addUserToShoppingList(shoppingListId: String, username: String, onFailure: () -> Unit) {
        viewModelScope.launch {
            val result = userRepository.addUserToShoppingList(shoppingListId, username)
            result.onSuccess { addedUser ->
                if(!_listUser.value.any{it.id==addedUser.id}) {
                    _listUser.value += addedUser
                }
            }
            result.onFailure {
                onFailure()
            }
        }
    }

    fun removeUserFromShoppingList(shoppingListId: String, userId: String) {
        if (userId == _currentUserId.value) return // prevents self-removal
        viewModelScope.launch {
            val result = userRepository.removeUserFromShoppingList(shoppingListId, userId)
            result.onSuccess {
                _listUser.value = _listUser.value.filter {it.id != userId}
            }
        }
    }
}

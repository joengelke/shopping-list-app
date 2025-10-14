package com.joengelke.shoppinglistapp.frontend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joengelke.shoppinglistapp.frontend.common.exception.UserException
import com.joengelke.shoppinglistapp.frontend.models.User
import com.joengelke.shoppinglistapp.frontend.network.TokenManager
import com.joengelke.shoppinglistapp.frontend.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    // all users in all shoppingLists
    private val _allUsers = MutableStateFlow<List<User>>(emptyList())
    val allUsers: StateFlow<List<User>> = _allUsers.asStateFlow()

    // users with access to specific shoppingList
    private val _listUser = MutableStateFlow<List<User>>(emptyList())
    val listUser: StateFlow<List<User>> = _listUser.asStateFlow()

    // current logged in user
    private val _currentUserId = MutableStateFlow<String>("")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    private val _currentRoles = MutableStateFlow<List<String>>(emptyList())
    val currentRoles: StateFlow<List<String>> = _currentRoles.asStateFlow()

    private val _currentUsername = MutableStateFlow<String>("")
    val currentUsername: StateFlow<String> = _currentUsername.asStateFlow()

    private val _currentUserRecipeIds = MutableStateFlow<List<String>>(emptyList())
    val currentUserRecipeIds: StateFlow<List<String>> = _currentUserRecipeIds.asStateFlow()

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

    fun updateUserRoles(
        isAdmin: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            val token = tokenManager.getToken()
            try {
                val roles = token?.let { tokenManager.getAuthoritiesFromToken(it) }
                _currentRoles.value = roles ?: emptyList()
                isAdmin(roles?.contains("ROLE_ADMIN") ?: false)
            } catch (e: Exception) {
                _currentRoles.value = emptyList()
            }
        }
    }

    fun updateUsername() {
        viewModelScope.launch {
            val token = tokenManager.getToken()
            try {
                _currentUsername.value = token?.let { tokenManager.getUsernameFromToken(it) } ?: ""
            } catch (e: Exception) {
                _currentUsername.value = ""
            }
        }
    }

    fun getAllUsers(
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val result = userRepository.getAllUsers()
            result.onSuccess { users ->
                _allUsers.value = users
                onSuccess()
            }
        }
    }

    fun getShoppingListUser(
        shoppingListId: String
    ) {
        viewModelScope.launch {
            val result = userRepository.getShoppingListUser(shoppingListId)
            result.onSuccess { users ->
                _listUser.value = users
            }
        }
    }

    fun getCurrentUserRecipeIds() {
        viewModelScope.launch {
            val result = userRepository.getCurrentUserRecipeIds()
            result.onSuccess { recipeIds ->
                _currentUserRecipeIds.value = recipeIds
            }
        }
    }

    fun addUserToShoppingList(
        shoppingListId: String,
        username: String,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        viewModelScope.launch {
            val result = userRepository.addUserToShoppingList(shoppingListId, username)
            result.onSuccess { addedUser ->
                if (!_listUser.value.any { it.id == addedUser.id }) {
                    _listUser.value += addedUser
                }
                onSuccess()
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
                _listUser.value = _listUser.value.filter { it.id != userId }
            }
        }
    }

    fun changeUsername(
        newUsername: String,
        usernameTaken: () -> Unit,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch {
            val result = userRepository.changeUsername(newUsername)
            result.onSuccess {
                _currentUsername.value = it.username
                onSuccess()
            }
            result.onFailure { e ->
                when (e) {
                    is UserException.UsernameTakenException -> usernameTaken()
                }
            }
        }
    }

    fun changePassword(
        currentPassword: String,
        newPassword: String,
        samePassword: () -> Unit,
        incorrectCurrentPassword: () -> Unit,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch {
            val result = userRepository.changePassword(currentPassword, newPassword)
            result.onSuccess {
                _currentUsername.value = it.username
                onSuccess()
            }
            result.onFailure { e ->
                when (e) {
                    is UserException.SamePasswordException -> samePassword()
                    is UserException.IncorrectCurrentPasswordException -> incorrectCurrentPassword()
                }
            }
        }
    }

    fun addRoleToUser(
        userId: String,
        role: String
    ) {
        viewModelScope.launch {
            val result = userRepository.addRoleToUser(userId, role)
            result.onSuccess { updatedUser ->
                _allUsers.update { users ->
                    users.map { user ->
                        if (user.id == updatedUser.id) updatedUser else user
                    }
                }
            }
        }
    }

    fun removeRoleFromUser(
        userId: String,
        role: String
    ) {
        viewModelScope.launch {
            val result = userRepository.removeRoleFromUser(userId, role)
            result.onSuccess { updatedUser ->
                _allUsers.update { users ->
                    users.map { user ->
                        if (user.id == updatedUser.id) updatedUser else user
                    }
                }
            }
        }
    }

    fun deleteUser(
        userId: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val result = userRepository.deleteUser(userId)
            result.onSuccess {
                _allUsers.update { users ->
                    users.filter { it.id != userId }
                }
                onSuccess()
            }
        }
    }
}

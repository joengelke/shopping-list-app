package com.joengelke.shoppinglistapp.frontend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joengelke.shoppinglistapp.frontend.models.User
import com.joengelke.shoppinglistapp.frontend.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository
): ViewModel() {

    // all users in all shoppingLists
    private val _user = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _user.asStateFlow()

    // users with access to specific shoppingList
    private val _listUser = MutableStateFlow<List<User>>(emptyList())
    val listUser: StateFlow<List<User>> = _listUser.asStateFlow()

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
        viewModelScope.launch {
            val result = userRepository.removeUserFromShoppingList(shoppingListId, userId)
            result.onSuccess {
                _listUser.value = _listUser.value.filter {it.id != userId}
            }
        }
    }

}

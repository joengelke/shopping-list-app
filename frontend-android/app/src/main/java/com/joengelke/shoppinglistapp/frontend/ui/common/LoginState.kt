package com.joengelke.shoppinglistapp.frontend.ui.common

sealed class LoginState {
    object Unknown : LoginState()       // initial/loading state
    object LoggedIn : LoginState()
    object LoggedOut : LoginState()
}

package com.joengelke.shoppinglistapp.frontend.common.exception

sealed class UserException(message: String): Exception(message) {
    class UsernameTakenException: UserException("Username already taken!")
    class IncorrectCurrentPasswordException : UserException("Current password is incorrect!")
    class SamePasswordException : UserException("Can't take the same password!")
}
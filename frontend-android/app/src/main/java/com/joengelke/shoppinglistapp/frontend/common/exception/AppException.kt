package com.joengelke.shoppinglistapp.frontend.common.exception

sealed class AppException(message: String): Exception(message) {
    class UnauthorizedException : AppException("Unauthorized")
    class NoTokenFoundException : AppException("No token found")
}
package com.joengelke.shoppinglistapp.frontend.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class AuthRequest(val username: String, val password: String)
data class LoginResponse(val token: String)
data class RegisterResponse(val username: String)

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: AuthRequest): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body request: AuthRequest): Response<RegisterResponse>
}

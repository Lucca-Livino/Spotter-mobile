package dev.fslab.academia.network

import dev.fslab.academia.model.GetSessionResponse
import dev.fslab.academia.model.LoginRequest
import dev.fslab.academia.model.LoginResponse
import dev.fslab.academia.model.RegisterRequest
import dev.fslab.academia.model.RegisterResponse
import dev.fslab.academia.model.MeResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/sign-in/email")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("auth/sign-up/email")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    @GET("auth/get-session")
    suspend fun getSession(): GetSessionResponse

    @GET("me")
    suspend fun getProfile(): MeResponse

    @POST("auth/sign-out")
    suspend fun logout()
}

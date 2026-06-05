package dev.fslab.academia.model

data class ForgetPasswordRequest(
    val email: String,
    val redirectTo: String = "academia://reset-password"
)

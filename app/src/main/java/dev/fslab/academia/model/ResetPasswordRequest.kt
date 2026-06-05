package dev.fslab.academia.model

data class ResetPasswordRequest(
    val newPassword: String,
    val token: String
)

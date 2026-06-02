package dev.fslab.academia.model

import com.google.gson.annotations.SerializedName

// ####################################################################################
//                       MODELOS DE REQUISIÇÕES
// ####################################################################################

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("rememberMe") val rememberMe: Boolean = true,
    @SerializedName("callbackURL") val callbackUrl: String = "/"
)

data class RegisterRequest(
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("confirmPassword") val confirmPassword: String? = null,
    @SerializedName("image") val image: String? = null,
    @SerializedName("callbackURL") val callbackUrl: String = "/"
)

data class SocialIdToken(
    @SerializedName("token") val token: String
)

data class SocialLoginRequest(
    @SerializedName("provider") val provider: String = "google",
    @SerializedName("callbackURL") val callbackUrl: String = "/",
    @SerializedName("idToken") val idToken: SocialIdToken
)

data class FcmTokenRequest(
    @SerializedName("fcm_token") val fcmToken: String
)

// ####################################################################################
//                       MODELOS DE RESPOSTAS
// ####################################################################################

data class SessionData(
    @SerializedName("id") val id: String? = null,
    @SerializedName("userId") val userId: String? = null,
    @SerializedName("token") val token: String? = null,
    @SerializedName("expiresAt") val expiresAt: String? = null
)

data class UserData(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("image") val image: String? = null,
    @SerializedName("tipo") val tipo: String? = null,
    @SerializedName("isAdmin") val isAdmin: Boolean? = null,
    @SerializedName("perfil") val perfil: com.google.gson.JsonElement? = null
)

data class LoginResponse(
    @SerializedName("token") val token: String? = null,
    @SerializedName("session") val session: SessionData? = null,
    @SerializedName("user") val user: UserData? = null
) {
    fun resolveToken(): String? = token ?: session?.token
}

data class RegisterResponse(
    @SerializedName("token") val token: String? = null,
    @SerializedName("session") val session: SessionData? = null,
    @SerializedName("user") val user: UserData? = null
) {
    fun resolveToken(): String? = token ?: session?.token
}

data class GetSessionResponse(
    @SerializedName("session") val session: SessionData? = null,
    @SerializedName("user") val user: UserData? = null
)

data class MeResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: UserData
)

fun UserData.toUser(): User {
    val tipoNormalizado = tipo?.trim()?.lowercase()
    
    val userTipo = when (tipoNormalizado) {
        "treinador" -> UserTipo.TREINADOR
        "aluno" -> UserTipo.ALUNO
        else -> UserTipo.ALUNO
    }
    
    return User(
        id = id,
        name = name,
        email = email,
        image = image.orEmpty(),
        tipo = userTipo,
        isAdmin = isAdmin ?: false,
        hasProfile = perfil != null && !perfil.isJsonNull
    )
}

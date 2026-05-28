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
    @SerializedName("tipo") val tipo: String = "aluno",
    @SerializedName("callbackURL") val callbackUrl: String = "/"
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
    @SerializedName("perfil") val perfil: com.google.gson.JsonObject? = null
)

data class LoginResponse(
    @SerializedName("error") val error: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("session") val session: SessionData? = null,
    @SerializedName("user") val user: UserData? = null
)

data class RegisterResponse(
    @SerializedName("error") val error: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("session") val session: SessionData? = null,
    @SerializedName("user") val user: UserData? = null
)

data class GetSessionResponse(
    @SerializedName("session") val session: SessionData? = null,
    @SerializedName("user") val user: UserData? = null
)

data class MeResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: UserData
)

fun UserData.toUser(): User {
    val userTipo = when (tipo?.lowercase()) {
        "treinador" -> UserTipo.TREINADOR
        else -> UserTipo.ALUNO
    }

    // Tentar extrair a URL da foto do objeto de perfil enriquecido
    val fotoPerfil = perfil?.get("url_foto")?.let {
        if (!it.isJsonNull) it.asString else null
    }

    return User(
        id = id,
        name = name,
        email = email,
        image = fotoPerfil ?: image ?: "",
        tipo = userTipo,
        isAdmin = isAdmin ?: false
    )
}

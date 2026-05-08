package dev.fslab.academia.model

enum class UserTipo {
    ALUNO,
    TREINADOR
}

data class User(
    val id: String,
    val name: String,
    val email: String,
    val image: String,
    val tipo: UserTipo = UserTipo.ALUNO,
    val isAdmin: Boolean = false
)

package dev.fslab.academia.model

import com.google.gson.annotations.SerializedName

/**
 * Representa o gênero do usuário conforme o ENUM do banco.
 */
enum class Genero(val valor: String) {
    @SerializedName("M") MASCULINO("M"),
    @SerializedName("F") FEMININO("F")
}

/**
 * Modelo completo para o perfil do Aluno, incluindo dados físicos (Figma Node 157:846).
 */
data class AlunoProfileData(
    @SerializedName("id") val id: String,
    @SerializedName("nome") val nome: String,
    @SerializedName("email") val email: String? = null,
    @SerializedName("data_nascimento") val dataNascimento: String? = null,
    @SerializedName("telefone") val telefone: String? = null,
    @SerializedName("sexo") val sexo: Genero? = null,
    @SerializedName("url_foto") val urlFoto: String? = null,
    @SerializedName("peso_atual_kg") val pesoKg: Double? = null,
    @SerializedName("altura_cm") val alturaCm: Int? = null,
    @SerializedName("academia_id") val academiaId: String? = null,
    @SerializedName("treinador_id") val treinadorId: String? = null,
    @SerializedName("academias") val academias: List<AcademiaData>? = emptyList()
)

/**
 * Modelo completo para o perfil do Treinador.
 */
data class TreinadorProfileData(
    @SerializedName("id") val id: String,
    @SerializedName("nome") val nome: String,
    @SerializedName("email") val email: String? = null,
    @SerializedName("data_nascimento") val dataNascimento: String? = null,
    @SerializedName("sexo") val sexo: Genero? = null,
    @SerializedName("url_foto") val urlFoto: String? = null,
    @SerializedName("cref") val cref: String? = null,
    @SerializedName("especializacao") val especializacao: String? = null,
    @SerializedName("graduacao") val graduacao: String? = null,
    @SerializedName("turnos") val turnos: List<String>? = emptyList(),
    @SerializedName("academia_id") val academiaId: String? = null,
    @SerializedName("academias") val academias: List<AcademiaData>? = emptyList()
)

/**
 * Respostas da API para perfis.
 */
data class AlunoProfileResponse(
    @SerializedName("error") val error: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: AlunoProfileData
)

data class TreinadorProfileResponse(
    @SerializedName("error") val error: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: TreinadorProfileData
)

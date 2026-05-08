package dev.fslab.academia.model

import com.google.gson.annotations.SerializedName

data class AlunoData(
    @SerializedName("id") val id: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("nome") val nome: String,
    @SerializedName("data_nascimento") val dataNascimento: String,
    @SerializedName("sexo") val sexo: String,
    @SerializedName("url_foto") val urlFoto: String? = null,
    @SerializedName("status_conta") val statusConta: Boolean = true,
    @SerializedName("academia_id") val academiaId: String,
    @SerializedName("treinador_id") val treinadorId: String? = null
)

data class AlunoPaginationData(
    @SerializedName("dados") val dados: List<AlunoData> = emptyList(),
    @SerializedName("total") val total: Int = 0,
    @SerializedName("page") val page: Int = 1,
    @SerializedName("limite") val limite: Int = 0,
    @SerializedName("totalPages") val totalPages: Int = 0
)

data class AlunoListResponse(
    @SerializedName("error") val error: Boolean = false,
    @SerializedName("code") val code: Int? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: AlunoPaginationData? = null,
    @SerializedName("errors") val errors: List<Map<String, Any?>> = emptyList()
)

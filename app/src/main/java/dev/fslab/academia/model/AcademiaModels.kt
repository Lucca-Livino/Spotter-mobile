package dev.fslab.academia.model

import com.google.gson.annotations.SerializedName

data class AcademiaData(
    @SerializedName("id") val id: String,
    @SerializedName("nome") val nome: String,
    @SerializedName("endereco_cidade") val cidade: String,
    @SerializedName("endereco_estado") val estado: String
)

data class AcademiaListResponse(
    @SerializedName("error") val error: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: AcademiaPagination
)

data class AcademiaPagination(
    @SerializedName("dados") val dados: List<AcademiaData>
)

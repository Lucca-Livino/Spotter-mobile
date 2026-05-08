package dev.fslab.academia.model

import com.google.gson.annotations.SerializedName

data class AparelhoData(
    @SerializedName("id") val id: String,
    @SerializedName("nome") val nome: String,
    @SerializedName("descricao") val descricao: String? = null
)

data class AparelhoPaginationData(
    @SerializedName("dados") val dados: List<AparelhoData> = emptyList(),
    @SerializedName("total") val total: Int = 0,
    @SerializedName("page") val page: Int = 1,
    @SerializedName("limite") val limite: Int = 0,
    @SerializedName("totalPages") val totalPages: Int = 0
)

data class AparelhoListResponse(
    @SerializedName("error") val error: Boolean = false,
    @SerializedName("code") val code: Int? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: AparelhoPaginationData? = null
)

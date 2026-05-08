package dev.fslab.academia.model

import com.google.gson.annotations.SerializedName

data class MusculoData(
    @SerializedName("id") val id: String,
    @SerializedName("nome") val nome: String,
    @SerializedName("grupo_muscular") val grupoMuscular: String
)

data class MusculoPaginationData(
    @SerializedName("dados") val dados: List<MusculoData> = emptyList(),
    @SerializedName("total") val total: Int = 0,
    @SerializedName("page") val page: Int = 1,
    @SerializedName("limite") val limite: Int = 0,
    @SerializedName("totalPages") val totalPages: Int = 0,
    @SerializedName("contagem_por_grupo") val contagemPorGrupo: Map<String, Int>? = null
)

data class MusculoListResponse(
    @SerializedName("error") val error: Boolean = false,
    @SerializedName("code") val code: Int? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: MusculoPaginationData? = null
)

enum class GrupoMuscular(val apiValue: String, val display: String) {
    PEITO("PEITO", "Peito"),
    COSTAS("COSTAS", "Costas"),
    PERNAS("PERNAS", "Pernas"),
    BRACOS("BRAÇOS", "Braços"),
    OMBROS("OMBROS", "Ombros"),
    ABDOMEN("ABDOMEN", "Abdômen"),
    PESCOCO("PESCOÇO", "Pescoço"),
    CARDIO("CARDIO", "Cardio");

    companion object {
        fun fromApi(value: String?): GrupoMuscular? = values().firstOrNull { it.apiValue == value }
    }
}

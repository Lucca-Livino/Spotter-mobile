package dev.fslab.academia.model

import com.google.gson.annotations.SerializedName

data class HistoricoPesoEntrada(
    @SerializedName("id") val id: String,
    @SerializedName("data_avaliacao") val dataAvaliacao: String,
    @SerializedName("peso_kg") val pesoKg: Double,
    @SerializedName("altura_cm") val alturaCm: Int? = null,
    @SerializedName("imc") val imc: Double? = null
)

data class HistoricoPesoMetricas(
    @SerializedName("peso_atual_kg") val pesoAtualKg: Double? = null,
    @SerializedName("peso_minimo_kg") val pesoMinimoKg: Double? = null,
    @SerializedName("peso_maximo_kg") val pesoMaximoKg: Double? = null,
    @SerializedName("variacao_total_kg") val variacaoTotalKg: Double? = null,
    @SerializedName("variacao_ultima_semana_kg") val variacaoUltimaSemanKg: Double? = null,
    @SerializedName("tendencia") val tendencia: String? = null,
    @SerializedName("total_registros") val totalRegistros: Int = 0
)

data class HistoricoPesoData(
    @SerializedName("entradas") val entradas: List<HistoricoPesoEntrada> = emptyList(),
    @SerializedName("metricas") val metricas: HistoricoPesoMetricas
)

data class HistoricoPesoResponse(
    @SerializedName("error") val error: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: HistoricoPesoData? = null
)

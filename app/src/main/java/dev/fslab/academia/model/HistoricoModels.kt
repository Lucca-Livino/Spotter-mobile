package dev.fslab.academia.model

import com.google.gson.annotations.SerializedName

data class EstatisticasData(
    @SerializedName("total_sessoes") val totalSessoes: Int,
    @SerializedName("sessoes_concluidas") val sessoesConcluidas: Int,
    @SerializedName("sessoes_canceladas") val sessoesCanceladas: Int,
    @SerializedName("tempo_total_minutos") val tempoTotalMinutos: Int,
    @SerializedName("media_duracao_minutos") val mediaDuracaoMinutos: Int,
    @SerializedName("volume_total_kg") val volumeTotalKg: Double,
    @SerializedName("sequencia_atual") val sequenciaAtual: Int,
    @SerializedName("melhor_sequencia") val melhorSequencia: Int,
    @SerializedName("treinos_por_semana_media") val treinosPorSemanaMedia: Double
)

data class GrupoMuscularData(
    @SerializedName("grupo_muscular") val grupoMuscular: String,
    @SerializedName("total_series") val totalSeries: Int,
    @SerializedName("volume_total_kg") val volumeTotalKg: Double,
    @SerializedName("percentual") val percentual: Double
)

data class ExercicioFrequenteData(
    @SerializedName("exercicio_id") val exercicioId: String,
    @SerializedName("nome") val nome: String,
    @SerializedName("total_sessoes") val totalSessoes: Int,
    @SerializedName("total_series") val totalSeries: Int,
    @SerializedName("volume_total_kg") val volumeTotalKg: Double
)

data class ProgressaoItemData(
    @SerializedName("data") val data: String,
    @SerializedName("sessao_id") val sessaoId: String,
    @SerializedName("maior_carga") val maiorCarga: Double?,
    @SerializedName("media_repeticoes") val mediaRepeticoes: Double?,
    @SerializedName("volume_total") val volumeTotal: Double
)

// Wrappers de resposta da API
data class EstatisticasResponse(
    @SerializedName("error") val error: Boolean = false,
    @SerializedName("data") val data: EstatisticasData? = null
)

data class GruposMusculareResponse(
    @SerializedName("error") val error: Boolean = false,
    @SerializedName("data") val data: List<GrupoMuscularData>? = null
)

data class ExerciciosFrequentesResponse(
    @SerializedName("error") val error: Boolean = false,
    @SerializedName("data") val data: List<ExercicioFrequenteData>? = null
)

data class ProgressaoResponse(
    @SerializedName("error") val error: Boolean = false,
    @SerializedName("data") val data: List<ProgressaoItemData>? = null
)

package dev.fslab.academia.model

import com.google.gson.annotations.SerializedName

data class EstatisticasData(
    @SerializedName("total_sessoes") val totalSessoes: Int,
    @SerializedName("sessoes_concluidas") val sessoesConcluidas: Int,
    @SerializedName("sessoes_canceladas") val sessoesCanceladas: Int,
    @SerializedName("tempo_total_minutos") val tempoTotalMinutos: Int,
    @SerializedName("media_duracao_minutos") val mediaDuracaoMinutos: Int,
    @SerializedName("volume_total_kg") val volumeTotalKg: Double,
    @SerializedName("tempo_total_isometria_segundos") val tempoTotalIsometriaSegundos: Int = 0,
    @SerializedName("media_tempo_isometria_segundos") val mediaTempoIsometriaSegundos: Int = 0,
    @SerializedName("sequencia_atual") val sequenciaAtual: Int,
    @SerializedName("melhor_sequencia") val melhorSequencia: Int,
    @SerializedName("treinos_por_semana_media") val treinosPorSemanaMedia: Double
)

data class GrupoMuscularData(
    @SerializedName("grupo_muscular") val grupoMuscular: String,
    @SerializedName("total_series") val totalSeries: Int,
    @SerializedName("volume_total_kg") val volumeTotalKg: Double,
    @SerializedName("tempo_total_segundos") val tempoTotalSegundos: Int = 0,
    @SerializedName("percentual") val percentual: Double
)

data class ExercicioFrequenteData(
    @SerializedName("exercicio_id") val exercicioId: String,
    @SerializedName("nome") val nome: String,
    @SerializedName("tipo_exercicio") val tipoExercicio: String = "REPETICAO",
    @SerializedName("total_sessoes") val totalSessoes: Int,
    @SerializedName("total_series") val totalSeries: Int,
    @SerializedName("volume_total_kg") val volumeTotalKg: Double,
    @SerializedName("tempo_total_segundos") val tempoTotalSegundos: Int = 0
) {
    val tipo: TipoExercicio get() = TipoExercicio.fromApi(tipoExercicio)
}

data class ProgressaoItemData(
    @SerializedName("data") val data: String,
    @SerializedName("sessao_id") val sessaoId: String,
    @SerializedName("tipo_exercicio") val tipoExercicio: String = "REPETICAO",
    @SerializedName("maior_carga") val maiorCarga: Double? = null,
    @SerializedName("media_repeticoes") val mediaRepeticoes: Double? = null,
    @SerializedName("volume_total") val volumeTotal: Double = 0.0,
    @SerializedName("melhor_tempo_segundos") val melhorTempoSegundos: Int? = null,
    @SerializedName("media_tempo_segundos") val mediaTempoSegundos: Int? = null,
    @SerializedName("tempo_total_segundos") val tempoTotalSegundos: Int = 0
) {
    val tipo: TipoExercicio get() = TipoExercicio.fromApi(tipoExercicio)
}

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

data class PeriodoComparativoData(
    @SerializedName("total_sessoes") val totalSessoes: Int = 0,
    @SerializedName("sessoes_concluidas") val sessoesConcluidas: Int = 0,
    @SerializedName("sessoes_canceladas") val sessoesCanceladas: Int = 0,
    @SerializedName("tempo_total_minutos") val tempoTotalMinutos: Int = 0,
    @SerializedName("media_duracao_minutos") val mediaDuracaoMinutos: Int = 0,
    @SerializedName("volume_total_kg") val volumeTotalKg: Double = 0.0,
    @SerializedName("tempo_total_isometria_segundos") val tempoTotalIsometriaSegundos: Int = 0,
    @SerializedName("distancia_total_metros") val distanciaTotalMetros: Double = 0.0,
    @SerializedName("treinos_por_semana_media") val treinosPorSemanaMedia: Double = 0.0
)

data class VariacaoComparativoData(
    @SerializedName("sessoes_concluidas_pct") val sessoesConcluídasPct: Double? = null,
    @SerializedName("sessoes_concluidas_abs") val sessoesConcluídasAbs: Int = 0,
    @SerializedName("volume_total_kg_pct") val volumeTotalKgPct: Double? = null,
    @SerializedName("volume_total_kg_abs") val volumeTotalKgAbs: Double = 0.0,
    @SerializedName("media_duracao_minutos_pct") val mediaDuracaoMinutosPct: Double? = null,
    @SerializedName("media_duracao_minutos_abs") val mediaDuracaoMinutosAbs: Double = 0.0,
    @SerializedName("treinos_por_semana_pct") val treinosPorSemanaPct: Double? = null,
    @SerializedName("treinos_por_semana_abs") val treinosPorSemanaAbs: Double = 0.0
)

data class ComparativoData(
    @SerializedName("periodo_atual_inicio") val periodoAtualInicio: String,
    @SerializedName("periodo_atual_fim") val periodoAtualFim: String,
    @SerializedName("periodo_anterior_inicio") val periodoAnteriorInicio: String,
    @SerializedName("periodo_anterior_fim") val periodoAnteriorFim: String,
    @SerializedName("periodo_atual") val periodoAtual: PeriodoComparativoData,
    @SerializedName("periodo_anterior") val periodoAnterior: PeriodoComparativoData,
    @SerializedName("variacao") val variacao: VariacaoComparativoData
)

data class ComparativoResponse(
    @SerializedName("error") val error: Boolean = false,
    @SerializedName("data") val data: ComparativoData? = null
)

data class RecordeExercicioData(
    @SerializedName("exercicio_id") val exercicioId: String,
    @SerializedName("nome") val nome: String,
    @SerializedName("tipo_exercicio") val tipoExercicio: String = "REPETICAO",
    @SerializedName("total_sessoes") val totalSessoes: Int = 0,
    @SerializedName("maior_carga_kg") val maiorCargaKg: Double? = null,
    @SerializedName("repeticoes_no_pr") val repeticoesNoPr: Int? = null,
    @SerializedName("data_pr_carga") val dataPrCarga: String? = null,
    @SerializedName("melhor_tempo_segundos") val melhorTempoSegundos: Int? = null,
    @SerializedName("data_pr_tempo") val dataPrTempo: String? = null,
    @SerializedName("maior_distancia_metros") val maiorDistanciaMetros: Double? = null,
    @SerializedName("data_pr_distancia") val dataPrDistancia: String? = null,
    @SerializedName("melhor_pace_segundos_por_km") val melhorPaceSegundosPorKm: Int? = null,
    @SerializedName("data_pr_pace") val dataPrPace: String? = null
) {
    val tipo: TipoExercicio get() = TipoExercicio.fromApi(tipoExercicio)
}

data class RecordeResponse(
    @SerializedName("error") val error: Boolean = false,
    @SerializedName("data") val data: RecordeExercicioData? = null
)

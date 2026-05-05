package dev.fslab.academia.model

import com.google.gson.annotations.SerializedName

data class SessaoSerieData(
    @SerializedName("id") val id: String,
    @SerializedName("numero_serie") val numeroSerie: Int,
    @SerializedName("repeticoes_realizadas") val repeticoesRealizadas: Int? = null,
    @SerializedName("carga_utilizada") val cargaUtilizada: String? = null,
    @SerializedName("status") val status: String = "PENDENTE",
    @SerializedName("observacoes") val observacoes: String? = null
)

data class SessaoExercicioInfo(
    @SerializedName("id") val id: String,
    @SerializedName("nome") val nome: String,
    @SerializedName("descricao") val descricao: String? = null,
    @SerializedName("animacao_url") val animacaoUrl: String? = null
)

data class SessaoExercicioTemplate(
    @SerializedName("series") val series: Int,
    @SerializedName("repeticoes") val repeticoes: String,
    @SerializedName("carga_sugerida") val cargaSugerida: String? = null,
    @SerializedName("tempo_descanso_segundos") val tempoDescansoSegundos: Int,
    @SerializedName("ordem_execucao") val ordemExecucao: Int
)

data class SessaoExercicioData(
    @SerializedName("id") val id: String,
    @SerializedName("treino_exercicio_id") val treinoExercicioId: String,
    @SerializedName("concluido") val concluido: Boolean = false,
    @SerializedName("observacoes") val observacoes: String? = null,
    @SerializedName("ordem") val ordem: Int,
    @SerializedName("inicio") val inicio: String? = null,
    @SerializedName("fim") val fim: String? = null,
    @SerializedName("exercicio") val exercicio: SessaoExercicioInfo,
    @SerializedName("template") val template: SessaoExercicioTemplate,
    @SerializedName("series") val series: List<SessaoSerieData> = emptyList()
)

data class SessaoData(
    @SerializedName("id") val id: String,
    @SerializedName("aluno_id") val alunoId: String,
    @SerializedName("treino_id") val treinoId: String,
    @SerializedName("status") val status: String,
    @SerializedName("inicio") val inicio: String? = null,
    @SerializedName("fim") val fim: String? = null,
    @SerializedName("observacoes") val observacoes: String? = null,
    @SerializedName("treino_nome") val treinoNome: String,
    @SerializedName("exercicios") val exercicios: List<SessaoExercicioData> = emptyList()
)

data class SessaoResumoData(
    @SerializedName("duracao_minutos") val duracaoMinutos: Int? = null,
    @SerializedName("exercicios_concluidos") val exerciciosConcluidos: Int,
    @SerializedName("exercicios_total") val exerciciosTotal: Int,
    @SerializedName("series_concluidas") val seriesConcluidas: Int,
    @SerializedName("series_total") val seriesTotal: Int,
    @SerializedName("volume_total_kg") val volumeTotalKg: Double,
    @SerializedName("taxa_conclusao") val taxaConclusao: Double
)

data class SessaoResponse(
    @SerializedName("error") val error: Boolean = false,
    @SerializedName("code") val code: Int? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: SessaoData? = null
)

data class SessaoResumoResponse(
    @SerializedName("error") val error: Boolean = false,
    @SerializedName("code") val code: Int? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: SessaoResumoData? = null
)

data class SessaoSerieItemRequest(
    @SerializedName("numero_serie") val numeroSerie: Int,
    @SerializedName("repeticoes_realizadas") val repeticoesRealizadas: Int?,
    @SerializedName("carga_utilizada") val cargaUtilizada: String?,
    @SerializedName("status") val status: String
)

data class SessaoSeriesUpdateRequest(
    @SerializedName("series") val series: List<SessaoSerieItemRequest>
)

data class SessaoExercicioUpdateRequest(
    @SerializedName("concluido") val concluido: Boolean
)

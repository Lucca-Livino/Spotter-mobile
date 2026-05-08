package dev.fslab.academia.model

import com.google.gson.annotations.SerializedName

enum class DiaSemana(val apiValue: String, val display: String, val curto: String) {
    SEGUNDA("SEGUNDA", "Segunda", "SEG"),
    TERCA("TERCA", "Terça", "TER"),
    QUARTA("QUARTA", "Quarta", "QUA"),
    QUINTA("QUINTA", "Quinta", "QUI"),
    SEXTA("SEXTA", "Sexta", "SEX"),
    SABADO("SABADO", "Sábado", "SAB"),
    DOMINGO("DOMINGO", "Domingo", "DOM");

    companion object {
        fun fromApi(value: String?): DiaSemana? = values().firstOrNull { it.apiValue == value }
    }
}

data class TreinoExercicioMusculoData(
    @SerializedName("musculo_id") val musculoId: String,
    @SerializedName("nome") val nome: String,
    @SerializedName("grupo_muscular") val grupoMuscular: String,
    @SerializedName("tipo_ativacao") val tipoAtivacao: String
)

data class TreinoExercicioAparelhoData(
    @SerializedName("aparelho_id") val aparelhoId: String,
    @SerializedName("nome") val nome: String,
    @SerializedName("descricao") val descricao: String? = null
)

data class TreinoExercicioInfo(
    @SerializedName("id") val id: String,
    @SerializedName("nome") val nome: String,
    @SerializedName("descricao") val descricao: String? = null,
    @SerializedName("musculos") val musculos: List<TreinoExercicioMusculoData> = emptyList(),
    @SerializedName("aparelhos") val aparelhos: List<TreinoExercicioAparelhoData> = emptyList()
)

data class TreinoExercicioDetalheData(
    @SerializedName("id") val id: String,
    @SerializedName("series") val series: Int,
    @SerializedName("repeticoes") val repeticoes: String,
    @SerializedName("carga_sugerida") val cargaSugerida: String? = null,
    @SerializedName("tempo_descanso_segundos") val tempoDescansoSegundos: Int,
    @SerializedName("ordem_execucao") val ordemExecucao: Int,
    @SerializedName("exercicio") val exercicio: TreinoExercicioInfo
)

data class TreinoData(
    @SerializedName("id") val id: String,
    @SerializedName("nome") val nome: String,
    @SerializedName("descricao") val descricao: String? = null,
    @SerializedName("data_criacao") val dataCriacao: String? = null,
    @SerializedName("deletado_em") val deletadoEm: String? = null,
    @SerializedName("usuario_id") val usuarioId: String? = null,
    @SerializedName("treinador_id") val treinadorId: String? = null,
    @SerializedName("dias_semana") val diasSemana: List<String>? = null,
    @SerializedName("ordem") val ordem: Int? = null,
    @SerializedName("exercicios") val exercicios: List<TreinoExercicioDetalheData> = emptyList()
)

data class TreinoPaginationData(
    @SerializedName("dados") val dados: List<TreinoData> = emptyList(),
    @SerializedName("total") val total: Int = 0,
    @SerializedName("page") val page: Int = 1,
    @SerializedName("limite") val limite: Int = 0,
    @SerializedName("totalPages") val totalPages: Int = 0
)

data class TreinoListResponse(
    @SerializedName("error") val error: Boolean = false,
    @SerializedName("code") val code: Int? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: TreinoPaginationData? = null,
    @SerializedName("errors") val errors: List<Map<String, Any?>> = emptyList()
)

data class TreinoDetailResponse(
    @SerializedName("error") val error: Boolean = false,
    @SerializedName("code") val code: Int? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: TreinoData? = null,
    @SerializedName("errors") val errors: List<Map<String, Any?>> = emptyList()
)

data class TreinoDeleteData(
    @SerializedName("treino") val treino: TreinoData? = null,
    @SerializedName("tipo_exclusao") val tipoExclusao: String? = null
)

data class TreinoDeleteResponse(
    @SerializedName("error") val error: Boolean = false,
    @SerializedName("code") val code: Int? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: TreinoDeleteData? = null,
    @SerializedName("errors") val errors: List<Map<String, Any?>> = emptyList()
)

data class TreinoExercicioItemRequest(
    @SerializedName("exercicio_id") val exercicioId: String,
    @SerializedName("series") val series: Int,
    @SerializedName("repeticoes") val repeticoes: String,
    @SerializedName("carga_sugerida") val cargaSugerida: Double? = null,
    @SerializedName("tempo_descanso_segundos") val tempoDescansoSegundos: Int,
    @SerializedName("ordem_execucao") val ordemExecucao: Int
)

data class CriarTreinoRequest(
    @SerializedName("nome") val nome: String,
    @SerializedName("descricao") val descricao: String? = null,
    @SerializedName("aluno_id") val alunoId: String? = null,
    @SerializedName("dias_semana") val diasSemana: List<String>? = null,
    @SerializedName("ordem") val ordem: Int? = null,
    @SerializedName("exercicios") val exercicios: List<TreinoExercicioItemRequest>? = null
)

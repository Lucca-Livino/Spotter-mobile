package dev.fslab.academia.model

import com.google.gson.annotations.SerializedName

data class ExercicioMusculoData(
    @SerializedName("musculo_id") val musculoId: String,
    @SerializedName("tipo_ativacao") val tipoAtivacao: String,
    @SerializedName("nome") val nome: String,
    @SerializedName("grupo_muscular") val grupoMuscular: String
)

data class ExercicioAparelhoData(
    @SerializedName("aparelho_id") val aparelhoId: String,
    @SerializedName("nome") val nome: String,
    @SerializedName("descricao") val descricao: String? = null
)

data class ExercicioData(
    @SerializedName("id") val id: String,
    @SerializedName("nome") val nome: String,
    @SerializedName("descricao") val descricao: String? = null,
    @SerializedName("animacao_url") val animacaoUrl: String? = null,
    @SerializedName("aluno_id") val alunoId: String? = null,
    @SerializedName("deletado_em") val deletadoEm: String? = null,
    @SerializedName("musculos") val musculos: List<ExercicioMusculoData> = emptyList(),
    @SerializedName("aparelhos") val aparelhos: List<ExercicioAparelhoData> = emptyList()
)

data class ExercicioPaginationData(
    @SerializedName("dados") val dados: List<ExercicioData> = emptyList(),
    @SerializedName("total") val total: Int = 0,
    @SerializedName("page") val page: Int = 1,
    @SerializedName("limite") val limite: Int = 0,
    @SerializedName("totalPages") val totalPages: Int = 0
)

data class ExercicioListResponse(
    @SerializedName("error") val error: Boolean = false,
    @SerializedName("code") val code: Int? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: ExercicioPaginationData? = null,
    @SerializedName("errors") val errors: List<Map<String, Any?>> = emptyList()
)

data class ExercicioDetailResponse(
    @SerializedName("error") val error: Boolean = false,
    @SerializedName("code") val code: Int? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: ExercicioData? = null,
    @SerializedName("errors") val errors: List<Map<String, Any?>> = emptyList()
)

data class ExercicioDeleteData(
    @SerializedName("exercicio") val exercicio: ExercicioData? = null,
    @SerializedName("tipo_exclusao") val tipoExclusao: String? = null
)

data class ExercicioDeleteResponse(
    @SerializedName("error") val error: Boolean = false,
    @SerializedName("code") val code: Int? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: ExercicioDeleteData? = null,
    @SerializedName("errors") val errors: List<Map<String, Any?>> = emptyList()
)

data class MusculoEntradaRequest(
    @SerializedName("musculo_id") val musculoId: String,
    @SerializedName("tipo_ativacao") val tipoAtivacao: String
)

data class AparelhoEntradaRequest(
    @SerializedName("aparelho_id") val aparelhoId: String
)

data class CriarExercicioRequest(
    @SerializedName("nome") val nome: String,
    @SerializedName("descricao") val descricao: String? = null,
    @SerializedName("animacao_url") val animacaoUrl: String? = null,
    @SerializedName("aluno_id") val alunoId: String? = null,
    @SerializedName("musculos") val musculos: List<MusculoEntradaRequest>,
    @SerializedName("aparelhos") val aparelhos: List<AparelhoEntradaRequest>? = null
)

data class AtualizarExercicioRequest(
    @SerializedName("nome") val nome: String? = null,
    @SerializedName("descricao") val descricao: String? = null,
    @SerializedName("animacao_url") val animacaoUrl: String? = null,
    @SerializedName("musculos") val musculos: List<MusculoEntradaRequest>? = null,
    @SerializedName("aparelhos") val aparelhos: List<AparelhoEntradaRequest>? = null
)

enum class EscopoExercicio(val apiValue: String) {
    GLOBAL("GLOBAL"),
    PESSOAL("PESSOAL"),
    TODOS("TODOS")
}

enum class TipoAtivacao { PRIMARIO, SECUNDARIO }

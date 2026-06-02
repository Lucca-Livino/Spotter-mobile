package dev.fslab.academia.model

import com.google.gson.annotations.SerializedName

data class ConversaData(
    @SerializedName("id") val id: String,
    @SerializedName("treinador_id") val treinadorId: String,
    @SerializedName("aluno_id") val alunoId: String,
    @SerializedName("ativa") val ativa: Boolean = true,
    @SerializedName("ultima_mensagem_em") val ultimaMensagemEm: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("mensagens_nao_lidas") val mensagensNaoLidas: Int = 0
)

data class ConversaPaginationData(
    @SerializedName("dados") val dados: List<ConversaData> = emptyList(),
    @SerializedName("total") val total: Int = 0,
    @SerializedName("page") val page: Int = 1,
    @SerializedName("limite") val limite: Int = 0,
    @SerializedName("totalPages") val totalPages: Int = 0
)

data class ConversaListResponse(
    @SerializedName("error") val error: Boolean = false,
    @SerializedName("code") val code: Int? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: ConversaPaginationData? = null,
    @SerializedName("errors") val errors: List<Map<String, Any?>> = emptyList()
)

data class ConversaDetailResponse(
    @SerializedName("error") val error: Boolean = false,
    @SerializedName("code") val code: Int? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: ConversaData? = null,
    @SerializedName("errors") val errors: List<Map<String, Any?>> = emptyList()
)

data class ConversaCriarRequest(
    @SerializedName("aluno_id") val alunoId: String? = null
)

data class MensagemConversaData(
    @SerializedName("id") val id: String,
    @SerializedName("conversa_id") val conversaId: String,
    @SerializedName("remetente_tipo") val remetenteTipo: String,
    @SerializedName("remetente_user_id") val remetenteUserId: String,
    @SerializedName("conteudo") val conteudo: String,
    @SerializedName("enviada_em") val enviadaEm: String,
    @SerializedName("lida_em") val lidaEm: String? = null,
    @SerializedName("lida_por_user_id") val lidaPorUserId: String? = null,
    @SerializedName("ativa") val ativa: Boolean = true
)

data class MensagemPaginationData(
    @SerializedName("dados") val dados: List<MensagemConversaData> = emptyList(),
    @SerializedName("total") val total: Int = 0,
    @SerializedName("page") val page: Int = 1,
    @SerializedName("limite") val limite: Int = 0,
    @SerializedName("totalPages") val totalPages: Int = 0
)

data class MensagemListResponse(
    @SerializedName("error") val error: Boolean = false,
    @SerializedName("code") val code: Int? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: MensagemPaginationData? = null,
    @SerializedName("errors") val errors: List<Map<String, Any?>> = emptyList()
)

data class MensagemDetailResponse(
    @SerializedName("error") val error: Boolean = false,
    @SerializedName("code") val code: Int? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: MensagemConversaData? = null,
    @SerializedName("errors") val errors: List<Map<String, Any?>> = emptyList()
)

data class EnviarMensagemRequest(
    @SerializedName("conteudo") val conteudo: String
)

data class MensagemLidasData(
    @SerializedName("marcadas") val marcadas: Int = 0
)

data class MensagemLidasResponse(
    @SerializedName("error") val error: Boolean = false,
    @SerializedName("code") val code: Int? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: MensagemLidasData? = null,
    @SerializedName("errors") val errors: List<Map<String, Any?>> = emptyList()
)

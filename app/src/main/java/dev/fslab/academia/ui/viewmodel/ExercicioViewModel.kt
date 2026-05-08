package dev.fslab.academia.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dev.fslab.academia.model.AtualizarExercicioRequest
import dev.fslab.academia.model.CriarExercicioRequest
import dev.fslab.academia.model.EscopoExercicio
import dev.fslab.academia.model.ExercicioData
import dev.fslab.academia.model.GrupoMuscular
import dev.fslab.academia.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.HttpException

sealed interface ExercicioListUiState {
    data object Idle : ExercicioListUiState
    data object Loading : ExercicioListUiState
    data object Empty : ExercicioListUiState
    data class Success(
        val exercicios: List<ExercicioData>,
        val total: Int,
        val page: Int,
        val totalPages: Int
    ) : ExercicioListUiState
    data class Error(val message: String) : ExercicioListUiState
}

sealed interface ExercicioDetalheUiState {
    data object Idle : ExercicioDetalheUiState
    data object Loading : ExercicioDetalheUiState
    data class Success(val exercicio: ExercicioData) : ExercicioDetalheUiState
    data class Error(val message: String) : ExercicioDetalheUiState
}

sealed interface ExercicioSalvarUiState {
    data object Idle : ExercicioSalvarUiState
    data object Loading : ExercicioSalvarUiState
    data class Success(val exercicio: ExercicioData) : ExercicioSalvarUiState
    data class Error(val message: String, val campo: String? = null) : ExercicioSalvarUiState
}

sealed interface ExercicioDeletarUiState {
    data object Idle : ExercicioDeletarUiState
    data object Loading : ExercicioDeletarUiState
    data class Success(val tipo: String) : ExercicioDeletarUiState
    data class Conflito(val message: String) : ExercicioDeletarUiState
    data class Error(val message: String) : ExercicioDeletarUiState
}

data class ExercicioFiltros(
    val busca: String = "",
    val grupoMuscular: GrupoMuscular? = null,
    val musculoIds: Set<String> = emptySet(),
    val aparelhoIds: Set<String> = emptySet(),
    val escopo: EscopoExercicio = EscopoExercicio.TODOS,
    val emUso: Boolean? = null
)

class ExercicioViewModel : ViewModel() {

    private val gson = Gson()

    private val _uiState = MutableStateFlow<ExercicioListUiState>(ExercicioListUiState.Idle)
    val uiState: StateFlow<ExercicioListUiState> = _uiState.asStateFlow()

    private val _filtros = MutableStateFlow(ExercicioFiltros())
    val filtros: StateFlow<ExercicioFiltros> = _filtros.asStateFlow()

    private val _detalheState = MutableStateFlow<ExercicioDetalheUiState>(ExercicioDetalheUiState.Idle)
    val detalheState: StateFlow<ExercicioDetalheUiState> = _detalheState.asStateFlow()

    private val _salvarState = MutableStateFlow<ExercicioSalvarUiState>(ExercicioSalvarUiState.Idle)
    val salvarState: StateFlow<ExercicioSalvarUiState> = _salvarState.asStateFlow()

    private val _deletarState = MutableStateFlow<ExercicioDeletarUiState>(ExercicioDeletarUiState.Idle)
    val deletarState: StateFlow<ExercicioDeletarUiState> = _deletarState.asStateFlow()

    fun atualizarFiltros(novo: ExercicioFiltros) {
        _filtros.value = novo
        carregar()
    }

    fun carregar(page: Int = 1) {
        val f = _filtros.value
        viewModelScope.launch {
            _uiState.value = ExercicioListUiState.Loading
            try {
                val resposta = RetrofitClient.exercicioApi.listar(
                    page = page,
                    limite = 20,
                    nome = f.busca.takeIf(String::isNotBlank),
                    grupoMuscular = f.grupoMuscular?.apiValue,
                    escopo = f.escopo.apiValue,
                    emUso = f.emUso,
                    incluirMusculos = true,
                    incluirAparelhos = true
                )
                val pagina = resposta.data
                val brutos = pagina?.dados.orEmpty()
                val temFiltroClient = f.musculoIds.isNotEmpty() || f.aparelhoIds.isNotEmpty()

                var lista = brutos
                if (f.musculoIds.isNotEmpty()) {
                    lista = lista.filter { ex -> ex.musculos.any { it.musculoId in f.musculoIds } }
                }
                if (f.aparelhoIds.isNotEmpty()) {
                    lista = lista.filter { ex -> ex.aparelhos.any { it.aparelhoId in f.aparelhoIds } }
                }

                _uiState.value = if (lista.isEmpty()) {
                    ExercicioListUiState.Empty
                } else if (temFiltroClient) {
                    ExercicioListUiState.Success(
                        exercicios = lista,
                        total = lista.size,
                        page = 1,
                        totalPages = 1
                    )
                } else {
                    ExercicioListUiState.Success(
                        exercicios = lista,
                        total = pagina?.total ?: lista.size,
                        page = pagina?.page ?: 1,
                        totalPages = pagina?.totalPages ?: 1
                    )
                }
            } catch (e: HttpException) {
                val apiMsg = e.response()?.errorBody()?.string()?.let(::extractApiErrorMessage)
                _uiState.value = ExercicioListUiState.Error(apiMsg ?: mapHttpError(e.code()))
            } catch (e: Exception) {
                _uiState.value = ExercicioListUiState.Error(e.message ?: "Sem conexão com a internet")
            }
        }
    }

    fun carregarDetalhe(id: String) {
        viewModelScope.launch {
            _detalheState.value = ExercicioDetalheUiState.Loading
            try {
                val resposta = RetrofitClient.exercicioApi.buscarPorId(id)
                val dado = resposta.data
                if (dado != null) {
                    _detalheState.value = ExercicioDetalheUiState.Success(dado)
                } else {
                    _detalheState.value = ExercicioDetalheUiState.Error(
                        resposta.message ?: "Exercício não encontrado"
                    )
                }
            } catch (e: HttpException) {
                val apiMsg = e.response()?.errorBody()?.string()?.let(::extractApiErrorMessage)
                _detalheState.value = ExercicioDetalheUiState.Error(apiMsg ?: mapHttpError(e.code()))
            } catch (e: Exception) {
                _detalheState.value = ExercicioDetalheUiState.Error(e.message ?: "Sem conexão com a internet")
            }
        }
    }

    fun resetDetalhe() { _detalheState.value = ExercicioDetalheUiState.Idle }
    fun resetSalvar() { _salvarState.value = ExercicioSalvarUiState.Idle }
    fun resetDeletar() { _deletarState.value = ExercicioDeletarUiState.Idle }

    fun criar(
        request: CriarExercicioRequest,
        animacaoBytes: ByteArray? = null,
        animacaoNome: String? = null,
        animacaoMimeType: String? = null
    ) {
        viewModelScope.launch {
            _salvarState.value = ExercicioSalvarUiState.Loading
            try {
                val dataPart = gson.toJson(request).toRequestBody("application/json".toMediaTypeOrNull())
                val animacaoPart = animacaoBytes?.let { bytes ->
                    val mime = animacaoMimeType ?: "video/webm"
                    val nome = animacaoNome ?: if (mime == "image/gif") "anim.gif" else "anim.webm"
                    val body = bytes.toRequestBody(mime.toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("animacao", nome, body)
                }
                val resposta = RetrofitClient.exercicioApi.criar(dataPart, animacaoPart)
                val criado = resposta.data
                if (criado != null) {
                    _salvarState.value = ExercicioSalvarUiState.Success(criado)
                } else {
                    _salvarState.value = ExercicioSalvarUiState.Error(
                        resposta.message ?: "Falha ao criar exercício"
                    )
                }
            } catch (e: HttpException) {
                _salvarState.value = mapearErroSalvar(e)
            } catch (e: Exception) {
                _salvarState.value = ExercicioSalvarUiState.Error(
                    e.message ?: "Sem conexão com a internet"
                )
            }
        }
    }

    fun atualizar(
        id: String,
        request: AtualizarExercicioRequest,
        removerAnimacao: Boolean = false,
        animacaoBytes: ByteArray? = null,
        animacaoNome: String? = null,
        animacaoMimeType: String? = null
    ) {
        viewModelScope.launch {
            _salvarState.value = ExercicioSalvarUiState.Loading
            try {
                val json = JSONObject()
                request.nome?.let { json.put("nome", it) }
                if (request.descricao != null) json.put("descricao", request.descricao)
                if (removerAnimacao) json.put("animacao_url", JSONObject.NULL)
                request.musculos?.let { lista ->
                    val arr = JSONArray()
                    lista.forEach { m ->
                        arr.put(JSONObject().apply {
                            put("musculo_id", m.musculoId)
                            put("tipo_ativacao", m.tipoAtivacao)
                        })
                    }
                    json.put("musculos", arr)
                }
                request.aparelhos?.let { lista ->
                    val arr = JSONArray()
                    lista.forEach { a ->
                        arr.put(JSONObject().apply { put("aparelho_id", a.aparelhoId) })
                    }
                    json.put("aparelhos", arr)
                }
                val dataPart = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
                val animacaoPart = animacaoBytes?.let { bytes ->
                    val mime = animacaoMimeType ?: "video/webm"
                    val nome = animacaoNome ?: if (mime == "image/gif") "anim.gif" else "anim.webm"
                    val body = bytes.toRequestBody(mime.toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("animacao", nome, body)
                }
                val resposta = RetrofitClient.exercicioApi.atualizar(id, dataPart, animacaoPart)
                val atualizado = resposta.data
                if (atualizado != null) {
                    _salvarState.value = ExercicioSalvarUiState.Success(atualizado)
                } else {
                    _salvarState.value = ExercicioSalvarUiState.Error(
                        resposta.message ?: "Falha ao atualizar exercício"
                    )
                }
            } catch (e: HttpException) {
                _salvarState.value = mapearErroSalvar(e)
            } catch (e: Exception) {
                _salvarState.value = ExercicioSalvarUiState.Error(
                    e.message ?: "Sem conexão com a internet"
                )
            }
        }
    }

    fun deletar(id: String, soft: Boolean? = null, force: Boolean? = null) {
        viewModelScope.launch {
            _deletarState.value = ExercicioDeletarUiState.Loading
            try {
                val resposta = RetrofitClient.exercicioApi.deletar(id, soft, force)
                val tipo = resposta.data?.tipoExclusao ?: "hard"
                _deletarState.value = ExercicioDeletarUiState.Success(tipo)
            } catch (e: HttpException) {
                val apiMsg = e.response()?.errorBody()?.string()?.let(::extractApiErrorMessage)
                _deletarState.value = if (e.code() == 409) {
                    ExercicioDeletarUiState.Conflito(apiMsg ?: "Exercício está em uso por treinos")
                } else {
                    ExercicioDeletarUiState.Error(apiMsg ?: mapHttpError(e.code()))
                }
            } catch (e: Exception) {
                _deletarState.value = ExercicioDeletarUiState.Error(
                    e.message ?: "Sem conexão com a internet"
                )
            }
        }
    }

    private fun mapearErroSalvar(e: HttpException): ExercicioSalvarUiState.Error {
        val raw = e.response()?.errorBody()?.string()
        val parsed = raw?.let { runCatching { JSONObject(it) }.getOrNull() }
        val mensagem = parsed?.optString("message")?.takeIf { it.isNotBlank() }
        val campoErro = parsed?.optJSONArray("errors")?.let { errs ->
            if (errs.length() > 0) {
                val primeiro = errs.optJSONObject(0)
                primeiro?.optJSONArray("path")?.optString(0)
            } else null
        }
        return ExercicioSalvarUiState.Error(
            message = mensagem ?: mapHttpError(e.code()),
            campo = campoErro
        )
    }

    private fun mapHttpError(code: Int): String = when (code) {
        401 -> "Sessão expirada. Faça login novamente"
        403 -> "Você não possui permissão para esta ação"
        404 -> "Exercício não encontrado"
        409 -> "Conflito: já existe um exercício com este nome"
        422 -> "Dados inválidos"
        in 500..599 -> "Servidor indisponível no momento"
        else -> "Falha na operação. Código HTTP: $code"
    }

    private fun extractApiErrorMessage(rawBody: String): String? {
        return runCatching {
            val json = JSONObject(rawBody)
            when {
                json.has("message") && !json.isNull("message") -> json.getString("message")
                json.has("error") -> json.getString("error")
                else -> null
            }
        }.getOrNull()?.takeIf { it.isNotBlank() }
    }
}

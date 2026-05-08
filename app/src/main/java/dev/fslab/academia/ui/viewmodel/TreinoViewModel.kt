package dev.fslab.academia.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.fslab.academia.model.DiaSemana
import dev.fslab.academia.model.TreinoData
import dev.fslab.academia.model.TreinoExercicioItemRequest
import dev.fslab.academia.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.HttpException

sealed interface TreinoListUiState {
    data object Idle : TreinoListUiState
    data object Loading : TreinoListUiState
    data object Empty : TreinoListUiState
    data class Success(
        val treinos: List<TreinoData>,
        val total: Int,
        val page: Int,
        val totalPages: Int
    ) : TreinoListUiState
    data class Error(val message: String) : TreinoListUiState
}

sealed interface TreinoDetalheUiState {
    data object Idle : TreinoDetalheUiState
    data object Loading : TreinoDetalheUiState
    data class Success(val treino: TreinoData) : TreinoDetalheUiState
    data class Error(val message: String) : TreinoDetalheUiState
}

sealed interface TreinoSalvarUiState {
    data object Idle : TreinoSalvarUiState
    data object Loading : TreinoSalvarUiState
    data class Success(val treino: TreinoData) : TreinoSalvarUiState
    data class Error(val message: String, val campo: String? = null) : TreinoSalvarUiState
}

sealed interface TreinoDeletarUiState {
    data object Idle : TreinoDeletarUiState
    data object Loading : TreinoDeletarUiState
    data class Success(val tipo: String) : TreinoDeletarUiState
    data class Error(val message: String) : TreinoDeletarUiState
}

sealed interface TreinoReorderUiState {
    data object Idle : TreinoReorderUiState
    data object Loading : TreinoReorderUiState
    data object Success : TreinoReorderUiState
    data class Error(val message: String) : TreinoReorderUiState
}

data class TreinoFiltros(
    val busca: String = "",
    val diasSemana: Set<DiaSemana> = emptySet(),
    val somenteComExercicios: Boolean = false
)

class TreinoViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<TreinoListUiState>(TreinoListUiState.Idle)
    val uiState: StateFlow<TreinoListUiState> = _uiState.asStateFlow()

    private val _filtros = MutableStateFlow(TreinoFiltros())
    val filtros: StateFlow<TreinoFiltros> = _filtros.asStateFlow()

    private val _detalheState = MutableStateFlow<TreinoDetalheUiState>(TreinoDetalheUiState.Idle)
    val detalheState: StateFlow<TreinoDetalheUiState> = _detalheState.asStateFlow()

    private val _salvarState = MutableStateFlow<TreinoSalvarUiState>(TreinoSalvarUiState.Idle)
    val salvarState: StateFlow<TreinoSalvarUiState> = _salvarState.asStateFlow()

    private val _deletarState = MutableStateFlow<TreinoDeletarUiState>(TreinoDeletarUiState.Idle)
    val deletarState: StateFlow<TreinoDeletarUiState> = _deletarState.asStateFlow()

    private val _reorderState = MutableStateFlow<TreinoReorderUiState>(TreinoReorderUiState.Idle)
    val reorderState: StateFlow<TreinoReorderUiState> = _reorderState.asStateFlow()

    fun atualizarFiltros(novo: TreinoFiltros) {
        _filtros.value = novo
        carregar()
    }

    fun carregar(page: Int = 1) {
        val f = _filtros.value
        viewModelScope.launch {
            _uiState.value = TreinoListUiState.Loading
            try {
                val resposta = RetrofitClient.treinoApi.listar(
                    page = page,
                    limite = 20,
                    nome = f.busca.takeIf(String::isNotBlank),
                    diasSemana = f.diasSemana
                        .takeIf { it.isNotEmpty() }
                        ?.joinToString(",") { it.apiValue },
                    somenteComExercicios = f.somenteComExercicios,
                    incluirExercicios = false,
                    ordemTreino = "asc"
                )
                val pagina = resposta.data
                val lista = pagina?.dados.orEmpty()
                _uiState.value = if (lista.isEmpty()) {
                    TreinoListUiState.Empty
                } else {
                    TreinoListUiState.Success(
                        treinos = lista,
                        total = pagina?.total ?: lista.size,
                        page = pagina?.page ?: 1,
                        totalPages = pagina?.totalPages ?: 1
                    )
                }
            } catch (e: HttpException) {
                val apiMsg = e.response()?.errorBody()?.string()?.let(::extractApiErrorMessage)
                _uiState.value = TreinoListUiState.Error(apiMsg ?: mapHttpError(e.code()))
            } catch (e: Exception) {
                _uiState.value = TreinoListUiState.Error(e.message ?: "Sem conexão com a internet")
            }
        }
    }

    fun carregarDetalhe(id: String) {
        viewModelScope.launch {
            _detalheState.value = TreinoDetalheUiState.Loading
            try {
                val resposta = RetrofitClient.treinoApi.buscarPorId(id)
                val dado = resposta.data
                if (dado != null) {
                    _detalheState.value = TreinoDetalheUiState.Success(dado)
                } else {
                    _detalheState.value = TreinoDetalheUiState.Error(
                        resposta.message ?: "Treino não encontrado"
                    )
                }
            } catch (e: HttpException) {
                val apiMsg = e.response()?.errorBody()?.string()?.let(::extractApiErrorMessage)
                _detalheState.value = TreinoDetalheUiState.Error(apiMsg ?: mapHttpError(e.code()))
            } catch (e: Exception) {
                _detalheState.value = TreinoDetalheUiState.Error(
                    e.message ?: "Sem conexão com a internet"
                )
            }
        }
    }

    fun resetDetalhe() { _detalheState.value = TreinoDetalheUiState.Idle }
    fun resetSalvar() { _salvarState.value = TreinoSalvarUiState.Idle }
    fun resetDeletar() { _deletarState.value = TreinoDeletarUiState.Idle }
    fun resetReorder() { _reorderState.value = TreinoReorderUiState.Idle }

    fun reordenar(idsParaOrdem: Map<String, Int>) {
        if (idsParaOrdem.isEmpty()) {
            _reorderState.value = TreinoReorderUiState.Success
            return
        }
        viewModelScope.launch {
            _reorderState.value = TreinoReorderUiState.Loading
            try {
                idsParaOrdem.forEach { (id, ordem) ->
                    val body = JSONObject()
                        .put("ordem", ordem)
                        .toString()
                        .toRequestBody("application/json".toMediaTypeOrNull())
                    RetrofitClient.treinoApi.atualizar(id, body)
                }
                _reorderState.value = TreinoReorderUiState.Success
                carregar()
            } catch (e: HttpException) {
                val apiMsg = e.response()?.errorBody()?.string()?.let(::extractApiErrorMessage)
                _reorderState.value = TreinoReorderUiState.Error(apiMsg ?: mapHttpError(e.code()))
            } catch (e: Exception) {
                _reorderState.value = TreinoReorderUiState.Error(
                    e.message ?: "Sem conexão com a internet"
                )
            }
        }
    }

    fun criar(
        nome: String,
        descricao: String? = null,
        diasSemana: List<DiaSemana> = emptyList(),
        ordem: Int? = null,
        exercicios: List<TreinoExercicioItemRequest> = emptyList()
    ) {
        viewModelScope.launch {
            _salvarState.value = TreinoSalvarUiState.Loading
            try {
                val json = JSONObject().apply {
                    put("nome", nome)
                    descricao?.let { put("descricao", it) }
                    if (diasSemana.isNotEmpty()) {
                        put("dias_semana", JSONArray(diasSemana.map { it.apiValue }))
                    }
                    ordem?.let { put("ordem", it) }
                    if (exercicios.isNotEmpty()) {
                        put("exercicios", arrayDeExercicios(exercicios))
                    }
                }
                val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
                val resposta = RetrofitClient.treinoApi.criar(body)
                val criado = resposta.data
                if (criado != null) {
                    _salvarState.value = TreinoSalvarUiState.Success(criado)
                } else {
                    _salvarState.value = TreinoSalvarUiState.Error(
                        resposta.message ?: "Falha ao criar treino"
                    )
                }
            } catch (e: HttpException) {
                _salvarState.value = mapearErroSalvar(e)
            } catch (e: Exception) {
                _salvarState.value = TreinoSalvarUiState.Error(
                    e.message ?: "Sem conexão com a internet"
                )
            }
        }
    }

    fun atualizar(
        id: String,
        nome: String?,
        descricao: String?,
        descricaoExplicitamenteNula: Boolean = false,
        diasSemana: List<DiaSemana>?,
        diasSemanaExplicitamenteNulo: Boolean = false,
        ordem: Int? = null,
        adicionar: List<TreinoExercicioItemRequest> = emptyList(),
        atualizar: List<TreinoExercicioPatchUpdate> = emptyList(),
        remover: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            _salvarState.value = TreinoSalvarUiState.Loading
            try {
                val json = JSONObject().apply {
                    nome?.let { put("nome", it) }
                    when {
                        descricaoExplicitamenteNula -> put("descricao", JSONObject.NULL)
                        descricao != null -> put("descricao", descricao)
                    }
                    when {
                        diasSemanaExplicitamenteNulo -> put("dias_semana", JSONObject.NULL)
                        diasSemana != null -> put("dias_semana", JSONArray(diasSemana.map { it.apiValue }))
                    }
                    ordem?.let { put("ordem", it) }
                    if (adicionar.isNotEmpty()) {
                        put("adicionar_exercicios", arrayDeExercicios(adicionar))
                    }
                    if (atualizar.isNotEmpty()) {
                        val arr = JSONArray()
                        atualizar.forEach { upd ->
                            arr.put(JSONObject().apply {
                                put("id", upd.id)
                                upd.series?.let { put("series", it) }
                                upd.repeticoes?.let { put("repeticoes", it) }
                                if (upd.cargaSugeridaExplicitamenteNula) {
                                    put("carga_sugerida", JSONObject.NULL)
                                } else {
                                    upd.cargaSugerida?.let { put("carga_sugerida", it) }
                                }
                                upd.tempoDescansoSegundos?.let { put("tempo_descanso_segundos", it) }
                                upd.ordemExecucao?.let { put("ordem_execucao", it) }
                            })
                        }
                        put("atualizar_exercicios", arr)
                    }
                    if (remover.isNotEmpty()) {
                        put("remover_exercicios_ids", JSONArray(remover))
                    }
                }
                val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
                val resposta = RetrofitClient.treinoApi.atualizar(id, body)
                val atualizado = resposta.data
                if (atualizado != null) {
                    _salvarState.value = TreinoSalvarUiState.Success(atualizado)
                } else {
                    _salvarState.value = TreinoSalvarUiState.Error(
                        resposta.message ?: "Falha ao atualizar treino"
                    )
                }
            } catch (e: HttpException) {
                _salvarState.value = mapearErroSalvar(e)
            } catch (e: Exception) {
                _salvarState.value = TreinoSalvarUiState.Error(
                    e.message ?: "Sem conexão com a internet"
                )
            }
        }
    }

    fun deletar(id: String, force: Boolean? = null) {
        viewModelScope.launch {
            _deletarState.value = TreinoDeletarUiState.Loading
            try {
                val resposta = RetrofitClient.treinoApi.deletar(id, force)
                val tipo = resposta.data?.tipoExclusao ?: "deleted"
                _deletarState.value = TreinoDeletarUiState.Success(tipo)
            } catch (e: HttpException) {
                val apiMsg = e.response()?.errorBody()?.string()?.let(::extractApiErrorMessage)
                _deletarState.value = TreinoDeletarUiState.Error(apiMsg ?: mapHttpError(e.code()))
            } catch (e: Exception) {
                _deletarState.value = TreinoDeletarUiState.Error(
                    e.message ?: "Sem conexão com a internet"
                )
            }
        }
    }

    private fun arrayDeExercicios(itens: List<TreinoExercicioItemRequest>): JSONArray {
        val arr = JSONArray()
        itens.forEach { item ->
            arr.put(JSONObject().apply {
                put("exercicio_id", item.exercicioId)
                put("series", item.series)
                put("repeticoes", item.repeticoes)
                item.cargaSugerida?.let { put("carga_sugerida", it) }
                put("tempo_descanso_segundos", item.tempoDescansoSegundos)
                put("ordem_execucao", item.ordemExecucao)
            })
        }
        return arr
    }

    private fun mapearErroSalvar(e: HttpException): TreinoSalvarUiState.Error {
        val raw = e.response()?.errorBody()?.string()
        val parsed = raw?.let { runCatching { JSONObject(it) }.getOrNull() }
        val mensagem = parsed?.optString("message")?.takeIf { it.isNotBlank() }
        val campoErro = parsed?.optJSONArray("errors")?.let { errs ->
            if (errs.length() > 0) {
                errs.optJSONObject(0)?.optJSONArray("path")?.optString(0)
            } else null
        }
        return TreinoSalvarUiState.Error(
            message = mensagem ?: mapHttpError(e.code()),
            campo = campoErro
        )
    }

    private fun mapHttpError(code: Int): String = when (code) {
        401 -> "Sessão expirada. Faça login novamente"
        403 -> "Você não possui permissão para esta ação"
        404 -> "Treino não encontrado"
        409 -> "Conflito: já existe um treino com este nome"
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

data class TreinoExercicioPatchUpdate(
    val id: String,
    val series: Int? = null,
    val repeticoes: String? = null,
    val cargaSugerida: Double? = null,
    val cargaSugeridaExplicitamenteNula: Boolean = false,
    val tempoDescansoSegundos: Int? = null,
    val ordemExecucao: Int? = null
)

package dev.fslab.academia.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.fslab.academia.model.AlunoData
import dev.fslab.academia.model.ConversaCriarRequest
import dev.fslab.academia.model.ConversaData
import dev.fslab.academia.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException

sealed interface ConversaListUiState {
    data object Idle : ConversaListUiState
    data object Loading : ConversaListUiState
    data object Empty : ConversaListUiState
    data class Success(val clientes: List<ConversaClienteUi>) : ConversaListUiState
    data class Error(val message: String) : ConversaListUiState
}

sealed interface ConversaIniciarUiState {
    data object Idle : ConversaIniciarUiState
    data object Loading : ConversaIniciarUiState
    data class Error(val message: String) : ConversaIniciarUiState
}

data class ConversaClienteUi(
    val alunoId: String,
    val nome: String,
    val fotoUrl: String? = null,
    val conversaId: String? = null,
    val ultimaMensagemEm: String? = null,
    val mensagensNaoLidas: Int = 0
)

class ConversasViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<ConversaListUiState>(ConversaListUiState.Idle)
    val uiState: StateFlow<ConversaListUiState> = _uiState.asStateFlow()

    private val _iniciarState = MutableStateFlow<ConversaIniciarUiState>(ConversaIniciarUiState.Idle)
    val iniciarState: StateFlow<ConversaIniciarUiState> = _iniciarState.asStateFlow()

    private val _navegarConversa = MutableStateFlow<String?>(null)
    val navegarConversa: StateFlow<String?> = _navegarConversa.asStateFlow()

    fun carregar() {
        viewModelScope.launch {
            _uiState.value = ConversaListUiState.Loading
            try {
                val alunosResponse = RetrofitClient.treinadorApi.listarAlunosVinculados(
                    page = 1,
                    limite = 100
                )
                val conversasResponse = RetrofitClient.conversaApi.listarConversas(
                    page = 1,
                    limite = 100
                )

                val alunos = alunosResponse.data?.dados.orEmpty()
                val conversas = conversasResponse.data?.dados.orEmpty()

                val clientes = mapClientes(alunos, conversas)
                _uiState.value = if (clientes.isEmpty()) {
                    ConversaListUiState.Empty
                } else {
                    ConversaListUiState.Success(clientes)
                }
            } catch (e: HttpException) {
                val apiMsg = e.response()?.errorBody()?.string()?.let(::extractApiErrorMessage)
                _uiState.value = ConversaListUiState.Error(apiMsg ?: mapHttpError(e.code()))
            } catch (e: Exception) {
                _uiState.value = ConversaListUiState.Error(
                    e.message ?: "Sem conexao com a internet"
                )
            }
        }
    }

    fun iniciarConversa(alunoId: String) {
        viewModelScope.launch {
            _iniciarState.value = ConversaIniciarUiState.Loading
            try {
                val resposta = RetrofitClient.conversaApi.iniciarOuBuscar(
                    ConversaCriarRequest(alunoId = alunoId)
                )
                val conversaId = resposta.data?.id
                if (!conversaId.isNullOrBlank()) {
                    _navegarConversa.value = conversaId
                    _iniciarState.value = ConversaIniciarUiState.Idle
                } else {
                    _iniciarState.value = ConversaIniciarUiState.Error(
                        resposta.message ?: "Nao foi possivel iniciar conversa"
                    )
                }
            } catch (e: HttpException) {
                val apiMsg = e.response()?.errorBody()?.string()?.let(::extractApiErrorMessage)
                _iniciarState.value = ConversaIniciarUiState.Error(apiMsg ?: mapHttpError(e.code()))
            } catch (e: Exception) {
                _iniciarState.value = ConversaIniciarUiState.Error(
                    e.message ?: "Sem conexao com a internet"
                )
            }
        }
    }

    fun consumirNavegacao() {
        _navegarConversa.value = null
    }

    private fun mapClientes(
        alunos: List<AlunoData>,
        conversas: List<ConversaData>
    ): List<ConversaClienteUi> {
        val conversasPorAluno = conversas.associateBy { it.alunoId }
        return alunos.map { aluno ->
            val conversa = conversasPorAluno[aluno.id]
            ConversaClienteUi(
                alunoId = aluno.id,
                nome = aluno.nome,
                fotoUrl = aluno.urlFoto,
                conversaId = conversa?.id,
                ultimaMensagemEm = conversa?.ultimaMensagemEm,
                mensagensNaoLidas = conversa?.mensagensNaoLidas ?: 0
            )
        }.sortedBy { it.nome }
    }

    private fun mapHttpError(code: Int): String = when (code) {
        400 -> "Dados invalidos."
        401 -> "Nao autorizado."
        403 -> "Acesso negado."
        429 -> "Muitas tentativas."
        in 500..599 -> "Erro no servidor."
        else -> "Falha ao carregar dados. Codigo HTTP: $code"
    }

    private fun extractApiErrorMessage(rawBody: String): String? {
        return runCatching {
            val json = JSONObject(rawBody)
            when {
                json.has("message") -> json.getString("message")
                json.has("error") -> json.getString("error")
                else -> null
            }
        }.getOrNull()?.takeIf { it.isNotBlank() }
    }
}

package dev.fslab.academia.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.fslab.academia.model.ConversaCriarRequest
import dev.fslab.academia.model.MensagemConversaData
import dev.fslab.academia.model.EnviarMensagemRequest
import dev.fslab.academia.network.ChatSocketManager
import dev.fslab.academia.network.RetrofitClient
import dev.fslab.academia.network.SessionStore
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException

sealed interface ChatMensagensUiState {
    data object Idle : ChatMensagensUiState
    data object Loading : ChatMensagensUiState
    data object Empty : ChatMensagensUiState
    data class Success(val mensagens: List<MensagemConversaData>) : ChatMensagensUiState
    data class Error(val message: String) : ChatMensagensUiState
}

sealed interface ChatEnviarUiState {
    data object Idle : ChatEnviarUiState
    data object Loading : ChatEnviarUiState
    data class Error(val message: String) : ChatEnviarUiState
}

class ChatViewModel : ViewModel() {

    private val _mensagensState = MutableStateFlow<ChatMensagensUiState>(ChatMensagensUiState.Idle)
    val mensagensState: StateFlow<ChatMensagensUiState> = _mensagensState.asStateFlow()

    private val _enviarState = MutableStateFlow<ChatEnviarUiState>(ChatEnviarUiState.Idle)
    val enviarState: StateFlow<ChatEnviarUiState> = _enviarState.asStateFlow()

    private val _conversaId = MutableStateFlow<String?>(null)
    val conversaId: StateFlow<String?> = _conversaId.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private var socketJob: Job? = null

    fun iniciarConversaAluno() {
        viewModelScope.launch {
            _mensagensState.value = ChatMensagensUiState.Loading
            try {
                val resposta = RetrofitClient.conversaApi.iniciarOuBuscar(ConversaCriarRequest())
                val id = resposta.data?.id
                if (id.isNullOrBlank()) {
                    _mensagensState.value = ChatMensagensUiState.Error(
                        resposta.message ?: "Nao foi possivel iniciar conversa"
                    )
                    return@launch
                }
                _conversaId.value = id
                carregarMensagens(id)
            } catch (e: HttpException) {
                val apiMsg = e.response()?.errorBody()?.string()?.let(::extractApiErrorMessage)
                _mensagensState.value = ChatMensagensUiState.Error(apiMsg ?: mapHttpError(e.code()))
            } catch (e: Exception) {
                _mensagensState.value = ChatMensagensUiState.Error(
                    e.message ?: "Sem conexao com a internet"
                )
            }
        }
    }

    fun carregarMensagens(conversaId: String) {
        _conversaId.value = conversaId
        viewModelScope.launch {
            _mensagensState.value = ChatMensagensUiState.Loading
            try {
                val resposta = RetrofitClient.conversaApi.listarMensagens(
                    conversaId = conversaId,
                    page = 1,
                    limite = 100
                )
                val mensagens = resposta.data?.dados.orEmpty()
                _mensagensState.value = if (mensagens.isEmpty()) {
                    ChatMensagensUiState.Empty
                } else {
                    ChatMensagensUiState.Success(mensagens)
                }
                marcarComoLidas(conversaId)
            } catch (e: HttpException) {
                val apiMsg = e.response()?.errorBody()?.string()?.let(::extractApiErrorMessage)
                _mensagensState.value = ChatMensagensUiState.Error(apiMsg ?: mapHttpError(e.code()))
            } catch (e: Exception) {
                _mensagensState.value = ChatMensagensUiState.Error(
                    e.message ?: "Sem conexao com a internet"
                )
            }
        }
    }

    fun enviarMensagem(conversaId: String, conteudo: String) {
        if (conteudo.isBlank()) return
        viewModelScope.launch {
            _enviarState.value = ChatEnviarUiState.Loading
            try {
                val resposta = RetrofitClient.conversaApi.enviarMensagem(
                    conversaId = conversaId,
                    request = EnviarMensagemRequest(conteudo = conteudo.trim())
                )
                val mensagem = resposta.data
                if (mensagem != null) {
                    adicionarMensagem(mensagem)
                    _enviarState.value = ChatEnviarUiState.Idle
                } else {
                    _enviarState.value = ChatEnviarUiState.Error(
                        resposta.message ?: "Nao foi possivel enviar mensagem"
                    )
                }
            } catch (e: HttpException) {
                val apiMsg = e.response()?.errorBody()?.string()?.let(::extractApiErrorMessage)
                _enviarState.value = ChatEnviarUiState.Error(apiMsg ?: mapHttpError(e.code()))
            } catch (e: Exception) {
                _enviarState.value = ChatEnviarUiState.Error(
                    e.message ?: "Sem conexao com a internet"
                )
            }
        }
    }

    fun bindSocket(conversaId: String) {
        if (conversaId.isBlank()) return
        
        val token = SessionStore.getToken()
        if (token.isNullOrBlank()) return

        ChatSocketManager.connect(token)
        ChatSocketManager.joinConversa(conversaId)

        socketJob?.cancel()
        socketJob = viewModelScope.launch {
            ChatSocketManager.mensagens.collect { mensagem ->
                if (mensagem.conversaId == conversaId) {
                    adicionarMensagem(mensagem)
                    marcarComoLidas(conversaId)
                } else {
                    _unreadCount.update { it + 1 }
                }
            }
        }
    }

    fun unbindSocket(conversaId: String) {
        ChatSocketManager.leaveConversa(conversaId)
        socketJob?.cancel()
        socketJob = null
    }

    private fun adicionarMensagem(mensagem: MensagemConversaData) {
        _mensagensState.update { state ->
            val atual = (state as? ChatMensagensUiState.Success)?.mensagens.orEmpty()
            if (atual.any { it.id == mensagem.id }) {
                state
            } else {
                val novaLista = atual + mensagem
                ChatMensagensUiState.Success(novaLista)
            }
        }
    }

    fun resetUnreadCount() {
        _unreadCount.value = 0
    }

    private suspend fun marcarComoLidas(conversaId: String) {
        runCatching { RetrofitClient.conversaApi.marcarComoLidas(conversaId) }
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

    override fun onCleared() {
        socketJob?.cancel()
        ChatSocketManager.disconnect()
        super.onCleared()
    }
}

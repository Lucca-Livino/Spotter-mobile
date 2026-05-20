package dev.fslab.academia.network

import com.google.gson.Gson
import dev.fslab.academia.model.MensagemConversaData
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.json.JSONObject

object ChatSocketManager {
    private val gson = Gson()
    private var socket: Socket? = null

    private val _mensagens = MutableSharedFlow<MensagemConversaData>(extraBufferCapacity = 64)
    val mensagens = _mensagens.asSharedFlow()

    fun connect(token: String?) {
        if (token.isNullOrBlank()) return
        if (socket?.connected() == true) return

        // Extrai a URL base sem o /api/ (ex: https://dominio.com)
        val baseUrl = RetrofitClient.BASE_URL.substringBefore("/api")
        
        val options = IO.Options().apply {
            auth = mapOf("token" to token)
            reconnection = true
            reconnectionAttempts = 5
            reconnectionDelay = 1000
            // Forçar Transports para evitar problemas de CORS/Proxy se necessário
            transports = arrayOf("websocket")
        }

        socket = IO.socket(baseUrl, options).apply {
            on(Socket.EVENT_CONNECT) {
                println("Socket conectado com sucesso")
            }
            on(Socket.EVENT_CONNECT_ERROR) { args ->
                println("Erro ao conectar socket: ${args.getOrNull(0)}")
            }
            on("mensagem:nova") { args ->
                val raw = args.firstOrNull() ?: return@on
                val json = when (raw) {
                    is JSONObject -> raw.toString()
                    else -> gson.toJson(raw)
                }
                runCatching { gson.fromJson(json, MensagemConversaData::class.java) }
                    .onSuccess { _mensagens.tryEmit(it) }
            }
            connect()
        }
    }

    fun joinConversa(conversaId: String) {
        socket?.emit("conversa:entrar", mapOf("conversaId" to conversaId))
    }

    fun leaveConversa(conversaId: String) {
        socket?.emit("conversa:sair", mapOf("conversaId" to conversaId))
    }

    fun disconnect() {
        socket?.disconnect()
        socket = null
    }
}

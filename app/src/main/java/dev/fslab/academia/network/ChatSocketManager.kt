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

    private var currentConversaId: String? = null

    fun connect(token: String?) {
        if (token.isNullOrBlank()) return
        if (socket != null) {
            if (socket?.connected() == false) socket?.connect()
            currentConversaId?.let { socket?.emit("conversa:entrar", JSONObject().apply { put("conversaId", it) }) }
            return
        }

        // Extrai a URL base sem o /api/ (ex: https://dominio.com)
        val baseUrl = RetrofitClient.BASE_URL.substringBefore("/api")
        
        val options = IO.Options().apply {
            auth = mapOf("token" to token)
            reconnection = true
            reconnectionAttempts = 5
            reconnectionDelay = 1000
        }

        socket = IO.socket(baseUrl, options).apply {
            on(Socket.EVENT_CONNECT) {
                println("Socket conectado com sucesso")
                currentConversaId?.let { socket?.emit("conversa:entrar", JSONObject().apply { put("conversaId", it) }) }
            }
            on(Socket.EVENT_CONNECT_ERROR) { args ->
                println("Erro ao conectar socket: ${args.getOrNull(0)}")
            }
            on("mensagem:nova") { args ->
                try {
                    val raw = args.firstOrNull() ?: return@on
                    val json = if (raw is JSONObject) raw.toString() else gson.toJson(raw)
                    val msg = gson.fromJson(json, MensagemConversaData::class.java)
                    _mensagens.tryEmit(msg)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            connect()
        }
    }

    fun joinConversa(conversaId: String) {
        currentConversaId = conversaId
        val data = JSONObject()
        data.put("conversaId", conversaId)
        socket?.emit("conversa:entrar", data)
    }

    fun leaveConversa(conversaId: String) {
        if (currentConversaId == conversaId) currentConversaId = null
        val data = JSONObject()
        data.put("conversaId", conversaId)
        socket?.emit("conversa:sair", data)
    }

    fun disconnect() {
        socket?.disconnect()
        socket = null
    }
}

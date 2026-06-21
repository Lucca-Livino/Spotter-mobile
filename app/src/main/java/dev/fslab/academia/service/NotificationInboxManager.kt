package dev.fslab.academia.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class NotificacaoLocal(
    val id: Long,
    val titulo: String,
    val corpo: String,
    val tipo: String,
    val timestamp: Long = System.currentTimeMillis(),
    val lida: Boolean = false
)

object NotificationInboxManager {
    private val _notifications = MutableStateFlow<List<NotificacaoLocal>>(emptyList())
    val notifications: StateFlow<List<NotificacaoLocal>> = _notifications.asStateFlow()

    val unreadCount: StateFlow<Int> get() = _unreadCount
    private val _unreadCount = MutableStateFlow(0)

    fun add(notif: NotificacaoLocal) {
        _notifications.update { listOf(notif) + it }
        _unreadCount.update { it + 1 }
    }

    fun markAllRead() {
        _notifications.update { list -> list.map { it.copy(lida = true) } }
        _unreadCount.value = 0
    }

    fun clear() {
        _notifications.value = emptyList()
        _unreadCount.value = 0
    }
}

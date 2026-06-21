package dev.fslab.academia.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate

object DeepLinkManager {
    private val _pendingRoute = MutableStateFlow<String?>(null)
    val pendingRoute: StateFlow<String?> = _pendingRoute.asStateFlow()

    fun setPendingRoute(route: String?) {
        _pendingRoute.value = route
    }

    fun consume(): String? = _pendingRoute.getAndUpdate { null }
}

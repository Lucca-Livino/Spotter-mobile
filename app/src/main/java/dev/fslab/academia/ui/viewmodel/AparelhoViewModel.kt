package dev.fslab.academia.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.fslab.academia.model.AparelhoData
import dev.fslab.academia.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AparelhoUiState {
    data object Idle : AparelhoUiState
    data object Loading : AparelhoUiState
    data class Success(val aparelhos: List<AparelhoData>) : AparelhoUiState
    data class Error(val message: String) : AparelhoUiState
}

class AparelhoViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<AparelhoUiState>(AparelhoUiState.Idle)
    val uiState: StateFlow<AparelhoUiState> = _uiState.asStateFlow()

    fun carregar(nome: String? = null) {
        viewModelScope.launch {
            _uiState.value = AparelhoUiState.Loading
            try {
                val resposta = RetrofitClient.aparelhoApi.listar(
                    nome = nome,
                    limite = 100
                )
                _uiState.value = AparelhoUiState.Success(resposta.data?.dados.orEmpty())
            } catch (e: Exception) {
                _uiState.value = AparelhoUiState.Error(e.message ?: "Falha ao carregar aparelhos")
            }
        }
    }
}

package dev.fslab.academia.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.fslab.academia.model.MusculoData
import dev.fslab.academia.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface MusculoUiState {
    data object Idle : MusculoUiState
    data object Loading : MusculoUiState
    data class Success(val musculos: List<MusculoData>) : MusculoUiState
    data class Error(val message: String) : MusculoUiState
}

class MusculoViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<MusculoUiState>(MusculoUiState.Idle)
    val uiState: StateFlow<MusculoUiState> = _uiState.asStateFlow()

    fun carregar(grupoMuscular: String? = null, nome: String? = null) {
        viewModelScope.launch {
            _uiState.value = MusculoUiState.Loading
            try {
                val resposta = RetrofitClient.musculoApi.listar(
                    grupoMuscular = grupoMuscular,
                    nome = nome,
                    limite = 100
                )
                _uiState.value = MusculoUiState.Success(resposta.data?.dados.orEmpty())
            } catch (e: Exception) {
                _uiState.value = MusculoUiState.Error(e.message ?: "Falha ao carregar músculos")
            }
        }
    }
}

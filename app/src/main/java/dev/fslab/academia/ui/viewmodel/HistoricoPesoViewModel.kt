package dev.fslab.academia.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.fslab.academia.model.HistoricoPesoData
import dev.fslab.academia.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HistoricoPesoUiState {
    data object Idle : HistoricoPesoUiState
    data object Loading : HistoricoPesoUiState
    data class Success(val data: HistoricoPesoData) : HistoricoPesoUiState
    data object Empty : HistoricoPesoUiState
    data class Error(val message: String) : HistoricoPesoUiState
}

class HistoricoPesoViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<HistoricoPesoUiState>(HistoricoPesoUiState.Idle)
    val uiState: StateFlow<HistoricoPesoUiState> = _uiState.asStateFlow()

    fun carregar(alunoId: String) {
        if (_uiState.value is HistoricoPesoUiState.Loading) return
        viewModelScope.launch {
            _uiState.value = HistoricoPesoUiState.Loading
            try {
                val resposta = RetrofitClient.profileApi.getHistoricoPeso(alunoId)
                val data = resposta.data
                if (data == null || data.entradas.isEmpty()) {
                    _uiState.value = HistoricoPesoUiState.Empty
                } else {
                    _uiState.value = HistoricoPesoUiState.Success(data)
                }
            } catch (e: Exception) {
                _uiState.value = HistoricoPesoUiState.Error(e.message ?: "Erro ao carregar histórico de peso")
            }
        }
    }
}

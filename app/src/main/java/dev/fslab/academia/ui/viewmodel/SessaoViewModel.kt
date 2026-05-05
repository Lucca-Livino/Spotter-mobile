package dev.fslab.academia.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.fslab.academia.model.SessaoData
import dev.fslab.academia.model.SessaoExercicioUpdateRequest
import dev.fslab.academia.model.SessaoResumoData
import dev.fslab.academia.model.SessaoSerieItemRequest
import dev.fslab.academia.model.SessaoSeriesUpdateRequest
import dev.fslab.academia.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.HttpException

sealed interface SessaoUiState {
    data object Idle : SessaoUiState
    data object Loading : SessaoUiState
    data class EmAndamento(val sessao: SessaoData) : SessaoUiState
    data class Finalizada(val sessao: SessaoData, val resumo: SessaoResumoData) : SessaoUiState
    data object SemSessaoAtiva : SessaoUiState
    data class Error(val message: String) : SessaoUiState
}

sealed interface SessaoSeriesUiState {
    data object Idle : SessaoSeriesUiState
    data object Loading : SessaoSeriesUiState
    data object Success : SessaoSeriesUiState
    data class Error(val message: String) : SessaoSeriesUiState
}

class SessaoViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<SessaoUiState>(SessaoUiState.Idle)
    val uiState: StateFlow<SessaoUiState> = _uiState.asStateFlow()

    private val _seriesState = MutableStateFlow<SessaoSeriesUiState>(SessaoSeriesUiState.Idle)
    val seriesState: StateFlow<SessaoSeriesUiState> = _seriesState.asStateFlow()

    fun iniciar(treinoId: String) {
        viewModelScope.launch {
            _uiState.value = SessaoUiState.Loading
            try {
                val body = JSONObject().put("treino_id", treinoId)
                    .toString()
                    .toRequestBody("application/json".toMediaTypeOrNull())
                val resposta = RetrofitClient.sessaoApi.iniciar(body)
                val sessao = resposta.body()?.data
                if (resposta.isSuccessful && sessao != null) {
                    _uiState.value = SessaoUiState.EmAndamento(sessao)
                } else if (resposta.code() == 409) {
                    verificarEmAndamento()
                } else {
                    _uiState.value = SessaoUiState.Error(
                        resposta.body()?.message ?: mapHttpError(resposta.code())
                    )
                }
            } catch (e: HttpException) {
                if (e.code() == 409) {
                    verificarEmAndamento()
                } else {
                    _uiState.value = SessaoUiState.Error(e.message ?: mapHttpError(e.code()))
                }
            } catch (e: Exception) {
                _uiState.value = SessaoUiState.Error(e.message ?: "Sem conexão com a internet")
            }
        }
    }

    fun verificarEmAndamento() {
        viewModelScope.launch {
            _uiState.value = SessaoUiState.Loading
            try {
                val resposta = RetrofitClient.sessaoApi.getEmAndamento()
                val sessao = resposta.body()?.data
                _uiState.value = if (resposta.isSuccessful && sessao != null) {
                    SessaoUiState.EmAndamento(sessao)
                } else {
                    SessaoUiState.SemSessaoAtiva
                }
            } catch (e: Exception) {
                _uiState.value = SessaoUiState.SemSessaoAtiva
            }
        }
    }

    fun registrarSeries(
        sessaoId: String,
        sessaoExercicioId: String,
        series: List<SessaoSerieItemRequest>
    ) {
        viewModelScope.launch {
            _seriesState.value = SessaoSeriesUiState.Loading
            try {
                val resposta = RetrofitClient.sessaoApi.updateSeries(
                    sessaoId,
                    sessaoExercicioId,
                    SessaoSeriesUpdateRequest(series)
                )
                if (resposta.isSuccessful) {
                    val sessaoAtualizada = resposta.body()?.data
                    if (sessaoAtualizada != null) {
                        _uiState.value = SessaoUiState.EmAndamento(sessaoAtualizada)
                    }
                    _seriesState.value = SessaoSeriesUiState.Success
                } else {
                    _seriesState.value = SessaoSeriesUiState.Error(mapHttpError(resposta.code()))
                }
            } catch (e: HttpException) {
                _seriesState.value = SessaoSeriesUiState.Error(mapHttpError(e.code()))
            } catch (e: Exception) {
                _seriesState.value = SessaoSeriesUiState.Error(e.message ?: "Sem conexão com a internet")
            }
        }
    }

    fun concluirExercicio(sessaoId: String, sessaoExercicioId: String) {
        viewModelScope.launch {
            try {
                val resposta = RetrofitClient.sessaoApi.updateExercicio(
                    sessaoId,
                    sessaoExercicioId,
                    SessaoExercicioUpdateRequest(concluido = true)
                )
                val sessaoAtualizada = resposta.body()?.data
                if (resposta.isSuccessful && sessaoAtualizada != null) {
                    _uiState.value = SessaoUiState.EmAndamento(sessaoAtualizada)
                }
            } catch (_: Exception) {}
        }
    }

    fun finalizar(sessaoId: String) {
        viewModelScope.launch {
            _uiState.value = SessaoUiState.Loading
            try {
                val finalizarResp = RetrofitClient.sessaoApi.finalizar(sessaoId)
                if (finalizarResp.isSuccessful) {
                    val sessao = finalizarResp.body()?.data
                    val resumoResp = RetrofitClient.sessaoApi.getResumo(sessaoId)
                    val resumo = resumoResp.body()?.data
                    if (sessao != null && resumo != null) {
                        _uiState.value = SessaoUiState.Finalizada(sessao, resumo)
                    } else {
                        _uiState.value = SessaoUiState.Error("Sessão finalizada, mas não foi possível carregar o resumo")
                    }
                } else {
                    _uiState.value = SessaoUiState.Error(
                        finalizarResp.body()?.message ?: mapHttpError(finalizarResp.code())
                    )
                }
            } catch (e: HttpException) {
                _uiState.value = SessaoUiState.Error(mapHttpError(e.code()))
            } catch (e: Exception) {
                _uiState.value = SessaoUiState.Error(e.message ?: "Sem conexão com a internet")
            }
        }
    }

    fun cancelar(sessaoId: String) {
        viewModelScope.launch {
            try {
                RetrofitClient.sessaoApi.cancelar(sessaoId)
                _uiState.value = SessaoUiState.SemSessaoAtiva
            } catch (_: Exception) {
                _uiState.value = SessaoUiState.SemSessaoAtiva
            }
        }
    }

    fun resetSeries() { _seriesState.value = SessaoSeriesUiState.Idle }

    private fun mapHttpError(code: Int): String = when (code) {
        401 -> "Sessão expirada. Faça login novamente"
        403 -> "Sem permissão para esta ação"
        404 -> "Sessão não encontrada"
        409 -> "Já existe uma sessão em andamento"
        422 -> "Dados inválidos"
        in 500..599 -> "Servidor indisponível"
        else -> "Erro HTTP: $code"
    }
}

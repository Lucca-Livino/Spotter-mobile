package dev.fslab.academia.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.fslab.academia.model.EstatisticasData
import dev.fslab.academia.model.ExercicioFrequenteData
import dev.fslab.academia.model.GrupoMuscularData
import dev.fslab.academia.model.ProgressaoItemData
import dev.fslab.academia.model.SessaoListItemData
import dev.fslab.academia.network.RetrofitClient
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

data class PeriodoFiltro(
    val dataInicio: String?,
    val dataFim: String?,
    val label: String
) {
    companion object {
        private val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC)

        private fun ultimos(dias: Int, label: String): PeriodoFiltro {
            val fim = Instant.now()
            val inicio = fim.minus(dias.toLong(), ChronoUnit.DAYS)
            return PeriodoFiltro(
                dataInicio = fmt.format(inicio),
                dataFim = fmt.format(fim),
                label = label
            )
        }

        fun seteDias() = ultimos(7, "7 dias")
        fun trintaDias() = ultimos(30, "30 dias")
        fun tresMeses() = ultimos(90, "3 meses")
        val TUDO = PeriodoFiltro(null, null, "Tudo")

        fun personalizado(dataInicioMillis: Long, dataFimMillis: Long): PeriodoFiltro {
            val inicio = Instant.ofEpochMilli(dataInicioMillis)
            val fim = Instant.ofEpochMilli(dataFimMillis)
            return PeriodoFiltro(fmt.format(inicio), fmt.format(fim), "Personalizado")
        }
    }
}

sealed interface HistoricoUiState {
    data object Loading : HistoricoUiState
    data class Success(
        val stats: EstatisticasData,
        val grupos: List<GrupoMuscularData>,
        val frequentes: List<ExercicioFrequenteData>,
        val sessoes: List<SessaoListItemData>,
        val totalPaginas: Int,
        val paginaAtual: Int
    ) : HistoricoUiState
    data class Error(val message: String) : HistoricoUiState
}

sealed interface SessoesCarregarMaisState {
    data object Idle : SessoesCarregarMaisState
    data object Loading : SessoesCarregarMaisState
    data object AllLoaded : SessoesCarregarMaisState
    data class Error(val message: String) : SessoesCarregarMaisState
}

sealed interface ProgressaoUiState {
    data object Loading : ProgressaoUiState
    data class Success(
        val exercicioNome: String,
        val progressao: List<ProgressaoItemData>
    ) : ProgressaoUiState
    data class Error(val message: String) : ProgressaoUiState
}

class HistoricoViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<HistoricoUiState>(HistoricoUiState.Loading)
    val uiState: StateFlow<HistoricoUiState> = _uiState.asStateFlow()

    private val _carregarMaisState = MutableStateFlow<SessoesCarregarMaisState>(SessoesCarregarMaisState.Idle)
    val carregarMaisState: StateFlow<SessoesCarregarMaisState> = _carregarMaisState.asStateFlow()

    private val _progressaoState = MutableStateFlow<ProgressaoUiState>(ProgressaoUiState.Loading)
    val progressaoState: StateFlow<ProgressaoUiState> = _progressaoState.asStateFlow()

    private val _periodoFiltro = MutableStateFlow(PeriodoFiltro.trintaDias())
    val periodoFiltro: StateFlow<PeriodoFiltro> = _periodoFiltro.asStateFlow()

    private val _filtroStatus = MutableStateFlow<String?>(null)
    val filtroStatus: StateFlow<String?> = _filtroStatus.asStateFlow()

    private var paginaAtual = 1
    private var totalPaginas = 1

    fun carregarHistorico(periodo: PeriodoFiltro = _periodoFiltro.value) {
        _uiState.value = HistoricoUiState.Loading
        paginaAtual = 1
        viewModelScope.launch {
            try {
                coroutineScope {
                    val statsDeferred = async {
                        RetrofitClient.historicoApi.getEstatisticas(
                            dataInicio = periodo.dataInicio,
                            dataFim = periodo.dataFim
                        )
                    }
                    val gruposDeferred = async {
                        RetrofitClient.historicoApi.getGruposMusculares(
                            dataInicio = periodo.dataInicio,
                            dataFim = periodo.dataFim
                        )
                    }
                    val frequentesDeferred = async {
                        RetrofitClient.historicoApi.getExerciciosFrequentes(
                            dataInicio = periodo.dataInicio,
                            dataFim = periodo.dataFim
                        )
                    }
                    val sessoesDeferred = async {
                        RetrofitClient.sessaoApi.listSessoes(
                            page = 1,
                            dataInicio = periodo.dataInicio,
                            dataFim = periodo.dataFim,
                            status = _filtroStatus.value
                        )
                    }

                    val statsResp = statsDeferred.await()
                    val gruposResp = gruposDeferred.await()
                    val frequentesResp = frequentesDeferred.await()
                    val sessoesResp = sessoesDeferred.await()

                    val stats = statsResp.body()?.data
                    val grupos = gruposResp.body()?.data ?: emptyList()
                    val frequentes = frequentesResp.body()?.data ?: emptyList()
                    val pagData = sessoesResp.body()?.data

                    if (stats != null) {
                        val pages = pagData?.totalPages ?: 1
                        totalPaginas = pages
                        _uiState.value = HistoricoUiState.Success(
                            stats = stats,
                            grupos = grupos,
                            frequentes = frequentes,
                            sessoes = pagData?.dados ?: emptyList(),
                            totalPaginas = pages,
                            paginaAtual = 1
                        )
                        _carregarMaisState.value = if (pages <= 1)
                            SessoesCarregarMaisState.AllLoaded
                        else
                            SessoesCarregarMaisState.Idle
                    } else {
                        val code = statsResp.code()
                        val detail = try { statsResp.errorBody()?.string()?.take(200) ?: "" } catch (_: Exception) { "" }
                        _uiState.value = HistoricoUiState.Error("HTTP $code: $detail")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = HistoricoUiState.Error(e.message ?: "Sem conexão com a internet")
            }
        }
    }

    fun carregarMaisSessoes() {
        val currentState = _uiState.value as? HistoricoUiState.Success ?: return
        if (paginaAtual >= totalPaginas) {
            _carregarMaisState.value = SessoesCarregarMaisState.AllLoaded
            return
        }
        val proximaPagina = paginaAtual + 1
        _carregarMaisState.value = SessoesCarregarMaisState.Loading
        val periodo = _periodoFiltro.value
        viewModelScope.launch {
            try {
                val resp = RetrofitClient.sessaoApi.listSessoes(
                    page = proximaPagina,
                    dataInicio = periodo.dataInicio,
                    dataFim = periodo.dataFim,
                    status = _filtroStatus.value
                )
                val pagData = resp.body()?.data
                if (resp.isSuccessful && pagData != null) {
                    paginaAtual = proximaPagina
                    _uiState.value = currentState.copy(
                        sessoes = currentState.sessoes + pagData.dados,
                        paginaAtual = proximaPagina
                    )
                    _carregarMaisState.value = if (proximaPagina >= pagData.totalPages)
                        SessoesCarregarMaisState.AllLoaded
                    else
                        SessoesCarregarMaisState.Idle
                } else {
                    _carregarMaisState.value = SessoesCarregarMaisState.Error("Erro ao carregar mais sessões")
                }
            } catch (e: Exception) {
                _carregarMaisState.value = SessoesCarregarMaisState.Error(e.message ?: "Sem conexão")
            }
        }
    }

    fun selecionarPeriodo(filtro: PeriodoFiltro) {
        _periodoFiltro.value = filtro
        carregarHistorico(filtro)
    }

    fun selecionarFiltroStatus(status: String?) {
        _filtroStatus.value = status
        val periodo = _periodoFiltro.value
        val currentState = _uiState.value as? HistoricoUiState.Success ?: return
        paginaAtual = 1
        _carregarMaisState.value = SessoesCarregarMaisState.Loading
        viewModelScope.launch {
            try {
                val resp = RetrofitClient.sessaoApi.listSessoes(
                    page = 1,
                    dataInicio = periodo.dataInicio,
                    dataFim = periodo.dataFim,
                    status = status
                )
                val pagData = resp.body()?.data
                if (resp.isSuccessful && pagData != null) {
                    totalPaginas = pagData.totalPages
                    _uiState.value = currentState.copy(
                        sessoes = pagData.dados,
                        totalPaginas = pagData.totalPages,
                        paginaAtual = 1
                    )
                    _carregarMaisState.value = if (pagData.totalPages <= 1)
                        SessoesCarregarMaisState.AllLoaded
                    else
                        SessoesCarregarMaisState.Idle
                }
            } catch (_: Exception) {
                _carregarMaisState.value = SessoesCarregarMaisState.Idle
            }
        }
    }

    fun carregarProgressao(exercicioId: String, exercicioNome: String, periodo: PeriodoFiltro = _periodoFiltro.value) {
        _progressaoState.value = ProgressaoUiState.Loading
        viewModelScope.launch {
            try {
                val resp = RetrofitClient.historicoApi.getProgressao(
                    exercicioId = exercicioId,
                    dataInicio = periodo.dataInicio,
                    dataFim = periodo.dataFim
                )
                val dados = resp.body()?.data
                if (resp.isSuccessful && dados != null) {
                    _progressaoState.value = ProgressaoUiState.Success(
                        exercicioNome = exercicioNome,
                        progressao = dados
                    )
                } else {
                    _progressaoState.value = ProgressaoUiState.Error("Sem dados de progressão")
                }
            } catch (e: Exception) {
                _progressaoState.value = ProgressaoUiState.Error(e.message ?: "Sem conexão com a internet")
            }
        }
    }
}

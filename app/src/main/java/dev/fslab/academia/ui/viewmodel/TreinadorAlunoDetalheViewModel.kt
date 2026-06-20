package dev.fslab.academia.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.fslab.academia.model.AlunoData
import dev.fslab.academia.model.EstatisticasData
import dev.fslab.academia.model.ExercicioFrequenteData
import dev.fslab.academia.model.GrupoMuscularData
import dev.fslab.academia.model.HistoricoPesoMetricas
import dev.fslab.academia.model.TreinoData
import dev.fslab.academia.network.RetrofitClient
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

sealed interface TreinadorAlunoDetalheUiState {
    data object Idle : TreinadorAlunoDetalheUiState
    data object Loading : TreinadorAlunoDetalheUiState
    class Success(
        val aluno: AlunoData,
        val treinos: List<TreinoData>,
        val diasTreino: Set<Int>,
        val ultimoTreino: LocalDate?
    ) : TreinadorAlunoDetalheUiState
    data class Error(val message: String) : TreinadorAlunoDetalheUiState
}

sealed interface DesvincularAlunoState {
    data object Idle : DesvincularAlunoState
    data object Loading : DesvincularAlunoState
    data object Success : DesvincularAlunoState
    data class Error(val message: String) : DesvincularAlunoState
}

enum class PeriodoEstatisticasAluno(val label: String, val dias: Int, val semanas: Int) {
    TRINTA_DIAS("30 dias", 30, 4),
    TRES_MESES("3 meses", 90, 12),
    SEIS_MESES("6 meses", 180, 26)
}

sealed interface AlunoEstatisticasUiState {
    data object Idle : AlunoEstatisticasUiState
    data object Loading : AlunoEstatisticasUiState
    data class Success(
        val stats: EstatisticasData,
        val grupos: List<GrupoMuscularData>,
        val frequentes: List<ExercicioFrequenteData>,
        val comparativo: ComparativoUiState,
        val historicoPeso: HistoricoPesoMetricas? = null
    ) : AlunoEstatisticasUiState
    data class Error(val message: String) : AlunoEstatisticasUiState
}

class TreinadorAlunoDetalheViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<TreinadorAlunoDetalheUiState>(TreinadorAlunoDetalheUiState.Idle)
    val uiState: StateFlow<TreinadorAlunoDetalheUiState> = _uiState.asStateFlow()

    private val _desvincularState = MutableStateFlow<DesvincularAlunoState>(DesvincularAlunoState.Idle)
    val desvincularState: StateFlow<DesvincularAlunoState> = _desvincularState.asStateFlow()

    private val _estatisticasState = MutableStateFlow<AlunoEstatisticasUiState>(AlunoEstatisticasUiState.Idle)
    val estatisticasState: StateFlow<AlunoEstatisticasUiState> = _estatisticasState.asStateFlow()

    private val _periodoEstatisticas = MutableStateFlow(PeriodoEstatisticasAluno.TRINTA_DIAS)
    val periodoEstatisticas: StateFlow<PeriodoEstatisticasAluno> = _periodoEstatisticas.asStateFlow()

    fun carregar(alunoId: String) {
        viewModelScope.launch {
            _uiState.value = TreinadorAlunoDetalheUiState.Loading
            try {
                // 1. Buscar todos os alunos vinculados para achar o específico
                // Idealmente teríamos um GET /alunos/{id}, mas usaremos a lista por enquanto
                val alunosResponse = RetrofitClient.treinadorApi.listarAlunosVinculados(limite = 100)
                val aluno = alunosResponse.data?.dados?.find { it.id == alunoId }
                
                if (aluno == null) {
                    _uiState.value = TreinadorAlunoDetalheUiState.Error("Aluno não encontrado")
                    return@launch
                }

                // 2. Buscar treinos desse aluno
                val treinosResponse = RetrofitClient.treinoApi.listar(
                    usuarioId = alunoId,
                    limite = 50,
                    incluirExercicios = false
                )
                val treinos = treinosResponse.data?.dados.orEmpty()

                // 3. Processar dias e último treino
                val diasTreino = mutableSetOf<Int>()
                var ultimoTreino: LocalDate? = null

                treinos.forEach { treino ->
                    treino.diasSemana.orEmpty().forEach { diaApi ->
                        mapDiaApiParaIndex(diaApi)?.let { diasTreino.add(it) }
                    }

                    val dataCriacao = treino.dataCriacao?.let(::parseApiDate)
                    if (dataCriacao != null) {
                        if (ultimoTreino == null || dataCriacao.isAfter(ultimoTreino)) {
                            ultimoTreino = dataCriacao
                        }
                    }
                }

                _uiState.value = TreinadorAlunoDetalheUiState.Success(
                    aluno = aluno,
                    treinos = treinos,
                    diasTreino = diasTreino,
                    ultimoTreino = ultimoTreino
                )
                carregarEstatisticas(alunoId)
            } catch (e: HttpException) {
                _uiState.value = TreinadorAlunoDetalheUiState.Error("Erro ao carregar dados do aluno")
            } catch (e: Exception) {
                _uiState.value = TreinadorAlunoDetalheUiState.Error(e.message ?: "Erro desconhecido")
            }
        }
    }

    fun selecionarPeriodoEstatisticas(alunoId: String, periodo: PeriodoEstatisticasAluno) {
        _periodoEstatisticas.value = periodo
        carregarEstatisticas(alunoId, periodo)
    }

    fun carregarEstatisticas(alunoId: String, periodo: PeriodoEstatisticasAluno = _periodoEstatisticas.value) {
        viewModelScope.launch {
            _estatisticasState.value = AlunoEstatisticasUiState.Loading
            try {
                val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC)
                val fim = Instant.now()
                val inicio = fim.minus(periodo.dias.toLong(), ChronoUnit.DAYS)
                val dataInicio = fmt.format(inicio)
                val dataFim = fmt.format(fim)

                coroutineScope {
                    val statsDeferred = async {
                        RetrofitClient.historicoApi.getEstatisticas(dataInicio = dataInicio, dataFim = dataFim, alunoId = alunoId)
                    }
                    val gruposDeferred = async {
                        RetrofitClient.historicoApi.getGruposMusculares(dataInicio = dataInicio, dataFim = dataFim, alunoId = alunoId)
                    }
                    val frequentesDeferred = async {
                        RetrofitClient.historicoApi.getExerciciosFrequentes(dataInicio = dataInicio, dataFim = dataFim, alunoId = alunoId)
                    }
                    val comparativoDeferred = async {
                        RetrofitClient.historicoApi.getComparativo(semanas = periodo.semanas, alunoId = alunoId)
                    }
                    val pesoDeferred = async {
                        runCatching { RetrofitClient.treinadorApi.getHistoricoPeso(alunoId) }.getOrNull()
                    }

                    val statsResp = statsDeferred.await()
                    val stats = statsResp.body()?.data
                    if (stats == null) {
                        _estatisticasState.value = AlunoEstatisticasUiState.Error("Erro ao carregar estatísticas (HTTP ${statsResp.code()})")
                        return@coroutineScope
                    }

                    val grupos = gruposDeferred.await().body()?.data ?: emptyList()
                    val frequentes = frequentesDeferred.await().body()?.data ?: emptyList()
                    val comparativoResp = comparativoDeferred.await()
                    val comparativoData = comparativoResp.body()?.data
                    val comparativoState = when {
                        !comparativoResp.isSuccessful || comparativoData == null ->
                            ComparativoUiState.Error("HTTP ${comparativoResp.code()}")
                        comparativoData.periodoAtual.sessoesConcluidas == 0 && comparativoData.periodoAnterior.sessoesConcluidas == 0 ->
                            ComparativoUiState.SemDados
                        else -> ComparativoUiState.Success(comparativoData)
                    }
                    val historicoPeso = pesoDeferred.await()?.body()?.data?.metricas

                    _estatisticasState.value = AlunoEstatisticasUiState.Success(
                        stats = stats,
                        grupos = grupos,
                        frequentes = frequentes,
                        comparativo = comparativoState,
                        historicoPeso = historicoPeso
                    )
                }
            } catch (e: Exception) {
                _estatisticasState.value = AlunoEstatisticasUiState.Error(e.message ?: "Sem conexão com a internet")
            }
        }
    }

    fun desvincularAluno(alunoId: String) {
        viewModelScope.launch {
            _desvincularState.value = DesvincularAlunoState.Loading
            try {
                RetrofitClient.profileApi.desvincularAluno(alunoId)
                _desvincularState.value = DesvincularAlunoState.Success
            } catch (e: Exception) {
                _desvincularState.value = DesvincularAlunoState.Error(e.message ?: "Erro ao desvincular")
            }
        }
    }

    fun resetDesvincular() {
        _desvincularState.value = DesvincularAlunoState.Idle
    }

    private fun mapDiaApiParaIndex(value: String): Int? = when (value.uppercase()) {
        "DOMINGO" -> 0
        "SEGUNDA" -> 1
        "TERCA" -> 2
        "QUARTA" -> 3
        "QUINTA" -> 4
        "SEXTA" -> 5
        "SABADO" -> 6
        else -> null
    }

    private fun parseApiDate(raw: String): LocalDate? {
        return runCatching { OffsetDateTime.parse(raw).toLocalDate() }.getOrNull()
            ?: runCatching { LocalDateTime.parse(raw).toLocalDate() }.getOrNull()
            ?: runCatching { LocalDate.parse(raw) }.getOrNull()
    }
}

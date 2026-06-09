package dev.fslab.academia.ui.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.fslab.academia.model.TreinoData
import dev.fslab.academia.network.RetrofitClient
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.time.DayOfWeek
import java.time.LocalDate

sealed interface AlunoVinculoState {
    data object Loading : AlunoVinculoState
    data object SemTreinador : AlunoVinculoState
    data class SolicitacaoPendente(val treinadorNome: String) : AlunoVinculoState
    data class ComTreinador(val treinadorId: String) : AlunoVinculoState
}

sealed interface HomeUiState {
    data object Idle : HomeUiState
    data object Loading : HomeUiState
    data class ComTreino(val treino: TreinoData) : HomeUiState
    data object SemTreino : HomeUiState
    data class Error(val message: String) : HomeUiState
}

// null = carregando, -1 = erro
data class HomeStreakState(val dias: Int?, val melhor: Int?)

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Idle)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _streak = MutableStateFlow(HomeStreakState(null, null))
    val streak: StateFlow<HomeStreakState> = _streak.asStateFlow()

    private val _vinculoState = MutableStateFlow<AlunoVinculoState>(AlunoVinculoState.Loading)
    val vinculoState: StateFlow<AlunoVinculoState> = _vinculoState.asStateFlow()

    @RequiresApi(Build.VERSION_CODES.O)
    fun carregarDados() {
        viewModelScope.launch {
            coroutineScope {
                val treinoJob = async { carregarTreinoDoDia() }
                val streakJob = async { carregarStreak() }
                val vinculoJob = async { carregarVinculo() }
                treinoJob.await()
                streakJob.await()
                vinculoJob.await()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun carregarTreinoDoDia() {
        _uiState.value = HomeUiState.Loading
        try {
            val resposta = RetrofitClient.treinoApi.listar(
                diasSemana = diaSemanaApiValor(),
                incluirExercicios = false,
                somenteComExercicios = true,
                ordemTreino = "asc",
                limite = 5
            )
            val treinos = resposta.data?.dados.orEmpty()
            _uiState.value = if (treinos.isEmpty()) {
                HomeUiState.SemTreino
            } else {
                HomeUiState.ComTreino(treinos.first())
            }
        } catch (e: HttpException) {
            _uiState.value = HomeUiState.Error(e.message ?: "Erro ao carregar treino do dia")
        } catch (e: Exception) {
            _uiState.value = HomeUiState.Error(e.message ?: "Sem conexão com a internet")
        }
    }

    private suspend fun carregarStreak() {
        try {
            val resp = RetrofitClient.historicoApi.getEstatisticas()
            val data = resp.body()?.data
            _streak.value = HomeStreakState(
                dias = data?.sequenciaAtual ?: 0,
                melhor = data?.melhorSequencia ?: 0
            )
        } catch (_: Exception) {
            _streak.value = HomeStreakState(dias = 0, melhor = 0)
        }
    }

    private suspend fun carregarVinculo() {
        try {
            val perfil = RetrofitClient.profileApi.getAlunoProfile()
            val treinadorId = perfil.data.treinadorId
            if (treinadorId != null) {
                _vinculoState.value = AlunoVinculoState.ComTreinador(treinadorId)
                return
            }
            try {
                val solicitacao = RetrofitClient.solicitacaoApi.buscarSolicitacaoDoAluno()
                val data = solicitacao.data
                if (data != null && data.status == "PENDENTE") {
                    _vinculoState.value = AlunoVinculoState.SolicitacaoPendente(
                        treinadorNome = data.treinador?.nome ?: "Treinador"
                    )
                } else {
                    _vinculoState.value = AlunoVinculoState.SemTreinador
                }
            } catch (_: Exception) {
                _vinculoState.value = AlunoVinculoState.SemTreinador
            }
        } catch (_: Exception) {
            _vinculoState.value = AlunoVinculoState.SemTreinador
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun diaSemanaApiValor(): String = when (LocalDate.now().dayOfWeek) {
        DayOfWeek.MONDAY -> "SEGUNDA"
        DayOfWeek.TUESDAY -> "TERCA"
        DayOfWeek.WEDNESDAY -> "QUARTA"
        DayOfWeek.THURSDAY -> "QUINTA"
        DayOfWeek.FRIDAY -> "SEXTA"
        DayOfWeek.SATURDAY -> "SABADO"
        DayOfWeek.SUNDAY -> "DOMINGO"
        else -> "SEGUNDA"
    }
}

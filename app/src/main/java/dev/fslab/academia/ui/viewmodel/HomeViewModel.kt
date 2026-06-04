package dev.fslab.academia.ui.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.fslab.academia.model.TreinoData
import dev.fslab.academia.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.time.DayOfWeek
import java.time.LocalDate

sealed interface HomeUiState {
    data object Idle : HomeUiState
    data object Loading : HomeUiState
    data class ComTreino(val treino: TreinoData) : HomeUiState
    data object SemTreino : HomeUiState
    data class Error(val message: String) : HomeUiState
}

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Idle)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    @RequiresApi(Build.VERSION_CODES.O)
    fun carregarTreinoDoDia() {
        viewModelScope.launch {
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

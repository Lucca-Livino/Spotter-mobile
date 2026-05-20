package dev.fslab.academia.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.fslab.academia.model.AlunoData
import dev.fslab.academia.model.TreinoData
import dev.fslab.academia.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime

sealed interface TreinadorAlunoDetalheUiState {
    data object Idle : TreinadorAlunoDetalheUiState
    data object Loading : TreinadorAlunoDetalheUiState
    data class Success(
        val aluno: AlunoData,
        val treinos: List<TreinoData>,
        val diasTreino: Set<Int>,
        val ultimoTreino: LocalDate?
    ) : TreinadorAlunoDetalheUiState
    data class Error(val message: String) : TreinadorAlunoDetalheUiState
}

class TreinadorAlunoDetalheViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<TreinadorAlunoDetalheUiState>(TreinadorAlunoDetalheUiState.Idle)
    val uiState: StateFlow<TreinadorAlunoDetalheUiState> = _uiState.asStateFlow()

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
            } catch (e: HttpException) {
                _uiState.value = TreinadorAlunoDetalheUiState.Error("Erro ao carregar dados do aluno")
            } catch (e: Exception) {
                _uiState.value = TreinadorAlunoDetalheUiState.Error(e.message ?: "Erro desconhecido")
            }
        }
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

package dev.fslab.academia.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.fslab.academia.model.AlunoData
import dev.fslab.academia.model.TreinoData
import dev.fslab.academia.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime

sealed interface TreinadorAlunosUiState {
    data object Idle : TreinadorAlunosUiState
    data object Loading : TreinadorAlunosUiState
    data object Empty : TreinadorAlunosUiState
    data class Success(
        val todosClientes: List<TreinadorClienteUi>,
        val clientesFiltrados: List<TreinadorClienteUi>
    ) : TreinadorAlunosUiState
    data class Error(val message: String) : TreinadorAlunosUiState
}

class TreinadorAlunosViewModel : ViewModel() {

    private val _rawState = MutableStateFlow<TreinadorAlunosUiState>(TreinadorAlunosUiState.Idle)
    private val _searchQuery = MutableStateFlow("")
    private val _diaFiltro = MutableStateFlow<Int?>(null)

    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    val diaFiltro: StateFlow<Int?> = _diaFiltro.asStateFlow()

    val uiState: StateFlow<TreinadorAlunosUiState> = combine(
        _rawState,
        _searchQuery,
        _diaFiltro
    ) { state, query, dia ->
        if (state is TreinadorAlunosUiState.Success) {
            val filtrados = state.todosClientes.filter { cliente ->
                val matchesQuery = query.isEmpty() || cliente.nome.contains(query, ignoreCase = true)
                val matchesDia = dia == null || cliente.diasTreino.contains(dia)
                matchesQuery && matchesDia
            }
            if (filtrados.isEmpty() && query.isEmpty() && dia == null) {
                TreinadorAlunosUiState.Empty
            } else {
                state.copy(clientesFiltrados = filtrados)
            }
        } else {
            state
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TreinadorAlunosUiState.Idle
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onDiaFiltroChange(dia: Int?) {
        _diaFiltro.value = if (_diaFiltro.value == dia) null else dia
    }

    fun carregar() {
        viewModelScope.launch {
            _rawState.value = TreinadorAlunosUiState.Loading
            try {
                val alunosResponse = RetrofitClient.treinadorApi.listarAlunosVinculados(
                    page = 1,
                    limite = 100
                )
                val alunos = alunosResponse.data?.dados.orEmpty()

                val treinosResponse = RetrofitClient.treinoApi.listar(
                    page = 1,
                    limite = 100,
                    incluirExercicios = false,
                    somenteComExercicios = false
                )
                val treinos = treinosResponse.data?.dados.orEmpty()

                val clientes = buildClientes(alunos, treinos)
                _rawState.value = if (clientes.isEmpty()) {
                    TreinadorAlunosUiState.Empty
                } else {
                    TreinadorAlunosUiState.Success(clientes, clientes)
                }
            } catch (e: HttpException) {
                val apiMsg = e.response()?.errorBody()?.string()?.let(::extractApiErrorMessage)
                _rawState.value = TreinadorAlunosUiState.Error(apiMsg ?: mapHttpError(e.code()))
            } catch (e: Exception) {
                _rawState.value = TreinadorAlunosUiState.Error(
                    e.message ?: "Sem conexão com a internet"
                )
            }
        }
    }

    private fun buildClientes(
        alunos: List<AlunoData>,
        treinos: List<TreinoData>
    ): List<TreinadorClienteUi> {
        val alunosMap = alunos.associateBy { it.id }
        val diasPorAluno = mutableMapOf<String, MutableSet<Int>>()
        val ultimoTreinoPorAluno = mutableMapOf<String, LocalDate>()

        treinos.forEach { treino ->
            val alunoId = treino.usuarioId ?: return@forEach
            if (!alunosMap.containsKey(alunoId)) return@forEach

            treino.diasSemana.orEmpty().forEach { diaApi ->
                val idx = mapDiaApiParaIndex(diaApi) ?: return@forEach
                diasPorAluno.getOrPut(alunoId) { mutableSetOf() }.add(idx)
            }

            val dataCriacao = treino.dataCriacao?.let(::parseApiDate)
            if (dataCriacao != null) {
                val atual = ultimoTreinoPorAluno[alunoId]
                if (atual == null || dataCriacao.isAfter(atual)) {
                    ultimoTreinoPorAluno[alunoId] = dataCriacao
                }
            }
        }

        return alunos.map { aluno ->
            TreinadorClienteUi(
                id = aluno.id,
                nome = aluno.nome,
                fotoUrl = aluno.urlFoto,
                diasTreino = diasPorAluno[aluno.id] ?: emptySet(),
                ultimoTreino = ultimoTreinoPorAluno[aluno.id]
            )
        }.sortedBy { it.nome }
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

    private fun mapHttpError(code: Int): String = when (code) {
        400 -> "Dados inválidos."
        401 -> "Não autorizado."
        403 -> "Acesso negado."
        429 -> "Muitas tentativas."
        in 500..599 -> "Erro no servidor."
        else -> "Falha ao carregar dados. Código HTTP: $code"
    }

    private fun extractApiErrorMessage(rawBody: String): String? {
        return runCatching {
            val json = org.json.JSONObject(rawBody)
            when {
                json.has("message") -> json.getString("message")
                json.has("error") -> json.getString("error")
                else -> null
            }
        }.getOrNull()?.takeIf { it.isNotBlank() }
    }
}

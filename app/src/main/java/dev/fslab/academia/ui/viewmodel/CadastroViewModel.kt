package dev.fslab.academia.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.fslab.academia.model.AcademiaData
import dev.fslab.academia.model.RegisterRequest
import dev.fslab.academia.model.UserTipo
import dev.fslab.academia.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

sealed interface CadastroUiState {
    data object DadosConta : CadastroUiState
    data object DadosPerfil : CadastroUiState
    data object Carregando : CadastroUiState
    data object Sucesso : CadastroUiState
    data class Erro(val mensagem: String) : CadastroUiState
}

class CadastroViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<CadastroUiState>(CadastroUiState.DadosConta)
    val uiState: StateFlow<CadastroUiState> = _uiState.asStateFlow()

    private val _academias = MutableStateFlow<List<AcademiaData>>(emptyList())
    val academias: StateFlow<List<AcademiaData>> = _academias.asStateFlow()

    // Dados temporários do passo 1
    var nome = ""
    var email = ""
    var senha = ""
    var tipo = UserTipo.ALUNO

    init {
        carregarAcademias()
    }

    private fun carregarAcademias() {
        viewModelScope.launch {
            try {
                android.util.Log.d("CadastroViewModel", "Iniciando busca de academias...")
                val response = RetrofitClient.academiaApi.getAcademias()
                if (!response.error) {
                    _academias.value = response.data.dados
                    android.util.Log.d("CadastroViewModel", "Academias carregadas: ${response.data.dados.size}")
                } else {
                    android.util.Log.e("CadastroViewModel", "Erro da API: ${response.message}")
                }
            } catch (e: Exception) {
                android.util.Log.e("CadastroViewModel", "Falha na rede: ${e.message}")
            }
        }
    }

    fun avancarParaPerfil() {
        if (nome.isNotBlank() && email.isNotBlank() && senha.isNotBlank()) {
            _uiState.value = CadastroUiState.DadosPerfil
        } else {
            _uiState.value = CadastroUiState.Erro("Preencha todos os campos da conta")
        }
    }

    fun voltarParaConta() {
        _uiState.value = CadastroUiState.DadosConta
    }

    fun finalizarCadastroAluno(
        dataNasc: String,
        sexo: String,
        academiaId: String,
        peso: Double?,
        altura: Double?
    ) {
        if (academiaId.isBlank()) {
            _uiState.value = CadastroUiState.Erro("Selecione uma unidade/academia")
            return
        }
        viewModelScope.launch {
            _uiState.value = CadastroUiState.Carregando
            try {
                // 1. Criar Conta Auth
                val regRequest = RegisterRequest(
                    name = nome,
                    email = email,
                    password = senha,
                    tipo = "aluno"
                )
                val authResponse = RetrofitClient.authApi.register(regRequest)
                
                if (authResponse.error) {
                    _uiState.value = CadastroUiState.Erro(authResponse.message ?: "Erro ao criar conta")
                    return@launch
                }

                val userId = authResponse.user?.id ?: throw Exception("Falha ao obter ID do usuário")

                // 2. Criar Perfil Aluno
                val profileJson = JSONObject().apply {
                    put("user_id", userId)
                    put("nome", nome)
                    put("data_nascimento", dataNasc)
                    put("sexo", sexo)
                    put("academia_id", academiaId)
                    if (peso != null) put("peso_atual_kg", peso)
                    if (altura != null) put("altura_m", altura)
                }
                
                val response = RetrofitClient.profileApi.createAlunoProfile(profileJson.toString())
                if (!response.error) {
                    _uiState.value = CadastroUiState.Sucesso
                } else {
                    _uiState.value = CadastroUiState.Erro(response.message ?: "Erro ao criar perfil")
                }
            } catch (e: Exception) {
                _uiState.value = CadastroUiState.Erro(e.message ?: "Erro ao realizar cadastro")
            }
        }
    }

    fun finalizarCadastroTreinador(
        dataNasc: String,
        sexo: String,
        academiaId: String,
        cref: String,
        graduacao: String,
        especializacao: String
    ) {
        if (academiaId.isBlank()) {
            _uiState.value = CadastroUiState.Erro("Selecione uma unidade/academia")
            return
        }
        viewModelScope.launch {
            _uiState.value = CadastroUiState.Carregando
            try {
                // 1. Criar Conta Auth
                val regRequest = RegisterRequest(
                    name = nome,
                    email = email,
                    password = senha,
                    tipo = "treinador"
                )
                val authResponse = RetrofitClient.authApi.register(regRequest)
                
                if (authResponse.error) {
                    _uiState.value = CadastroUiState.Erro(authResponse.message ?: "Erro ao criar conta")
                    return@launch
                }

                // 2. Criar Perfil Treinador
                val profileJson = JSONObject().apply {
                    put("nome", nome)
                    put("data_nascimento", dataNasc)
                    put("sexo", sexo)
                    put("academia_id", academiaId)
                    put("cref", cref)
                    put("graduacao", graduacao)
                    put("especializacao", especializacao)
                    put("turnos", org.json.JSONArray(listOf("MANHA", "TARDE", "NOITE")))
                }
                
                val response = RetrofitClient.profileApi.createTreinadorProfile(profileJson.toString())
                if (!response.error) {
                    _uiState.value = CadastroUiState.Sucesso
                } else {
                    _uiState.value = CadastroUiState.Erro(response.message ?: "Erro ao criar perfil")
                }
            } catch (e: Exception) {
                _uiState.value = CadastroUiState.Erro(e.message ?: "Erro ao realizar cadastro")
            }
        }
    }
}

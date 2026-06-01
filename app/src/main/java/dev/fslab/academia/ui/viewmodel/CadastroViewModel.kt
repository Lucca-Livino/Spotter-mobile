package dev.fslab.academia.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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

    // Dados temporários do passo 1 usando Compose states para sincronização bidirecional
    var nome by mutableStateOf("")
    var email by mutableStateOf("")
    var senha by mutableStateOf("")
    var tipo by mutableStateOf(UserTipo.ALUNO)
    var isSocial by mutableStateOf(false)

    init {
        carregarAcademias()
    }

    fun preencherDadosSociais(nomeSocial: String, emailSocial: String) {
        nome = nomeSocial
        email = emailSocial
        isSocial = true
        // Removido o salto direto para DadosPerfil para permitir a escolha entre Aluno/Treinador
        _uiState.value = CadastroUiState.DadosConta
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
        val contaValida = if (isSocial) {
            nome.isNotBlank() && email.isNotBlank()
        } else {
            nome.isNotBlank() && email.isNotBlank() && senha.isNotBlank()
        }

        if (contaValida) {
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
                var userIdFinal: String? = null
                
                if (!isSocial) {
                    // 1. Criar Conta Auth
                    val regRequest = RegisterRequest(
                        name = nome,
                        email = email,
                        password = senha
                    )
                    val authResponse = RetrofitClient.authApi.register(regRequest)
                    
                    if (authResponse.user == null) {
                        _uiState.value = CadastroUiState.Erro("Erro ao criar conta")
                        return@launch
                    }
                    userIdFinal = authResponse.user.id
                } else {
                    // Se for social, o usuário já está logado no BetterAuth.
                    // Buscamos o ID dele na sessão atual.
                    val session = RetrofitClient.authApi.getSession()
                    userIdFinal = session.user?.id
                }

                if (userIdFinal == null) {
                    _uiState.value = CadastroUiState.Erro("Não foi possível identificar seu usuário")
                    return@launch
                }

                // 2. Criar Perfil Aluno
                val profileJson = JSONObject().apply {
                    put("user_id", userIdFinal)
                    put("nome", nome)
                    put("data_nascimento", dataNasc)
                    put("sexo", sexo)
                    put("academia_id", academiaId)
                    // Enviar apenas se não for nulo
                    peso?.let { put("peso_atual_kg", it) }
                    altura?.let { put("altura_m", it) }
                }
                
                android.util.Log.d("CadastroViewModel", "Enviando perfil: ${profileJson.toString()}")
                
                val requestBody = profileJson.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val response = RetrofitClient.profileApi.createAlunoProfile(requestBody)
                if (!response.error) {
                    _uiState.value = CadastroUiState.Sucesso
                } else {
                    _uiState.value = CadastroUiState.Erro(response.message ?: "Erro ao criar perfil")
                }
            } catch (e: Exception) {
                android.util.Log.e("CadastroViewModel", "Erro no cadastro: ${e.message}")
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
                var userIdFinal: String? = null
                
                if (!isSocial) {
                    // 1. Criar Conta Auth
                    val regRequest = RegisterRequest(
                        name = nome,
                        email = email,
                        password = senha
                    )
                    val authResponse = RetrofitClient.authApi.register(regRequest)
                    
                    if (authResponse.user == null) {
                        _uiState.value = CadastroUiState.Erro("Erro ao criar conta")
                        return@launch
                    }
                    userIdFinal = authResponse.user.id
                } else {
                    // Se for social, o usuário já está logado
                    val session = RetrofitClient.authApi.getSession()
                    userIdFinal = session.user?.id
                }

                if (userIdFinal == null) {
                    _uiState.value = CadastroUiState.Erro("Não foi possível identificar seu usuário")
                    return@launch
                }

                // 2. Criar Perfil Treinador
                val profileJson = JSONObject().apply {
                    put("user_id", userIdFinal)
                    put("nome", nome)
                    put("data_nascimento", dataNasc)
                    put("sexo", sexo)
                    put("academia_id", academiaId)
                    put("cref", cref)
                    put("graduacao", graduacao)
                    put("especializacao", especializacao)
                    put("turnos", org.json.JSONArray(listOf("MANHA", "TARDE", "NOITE")))
                }
                
                android.util.Log.d("CadastroViewModel", "Enviando perfil treinador: ${profileJson.toString()}")

                val requestBody = profileJson.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val response = RetrofitClient.profileApi.createTreinadorProfile(requestBody)
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

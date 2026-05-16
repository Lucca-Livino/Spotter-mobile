package dev.fslab.academia.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.fslab.academia.model.AlunoProfileData
import dev.fslab.academia.model.TreinadorProfileData
import dev.fslab.academia.model.UserTipo
import dev.fslab.academia.network.RetrofitClient
import dev.fslab.academia.ui.util.FileUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

sealed interface PerfilUiState {
    data object Idle : PerfilUiState
    data object Loading : PerfilUiState
    data class SuccessAluno(val profile: AlunoProfileData) : PerfilUiState
    data class SuccessTreinador(val profile: TreinadorProfileData) : PerfilUiState
    data class Error(val message: String) : PerfilUiState
}

class PerfilViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<PerfilUiState>(PerfilUiState.Idle)
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating.asStateFlow()

    /**
     * Carrega o perfil do usuário logado baseado na rota unificada /me.
     */
    fun carregarPerfil(tipo: UserTipo) {
        viewModelScope.launch {
            _uiState.value = PerfilUiState.Loading
            try {
                val response = RetrofitClient.authApi.getProfile()
                val profileJson = response.data.perfil
                val gson = com.google.gson.Gson()

                if (profileJson == null || profileJson.isJsonNull) {
                    _uiState.value = PerfilUiState.Error("Perfil não encontrado no servidor")
                    return@launch
                }

                if (tipo == UserTipo.TREINADOR) {
                    val profileData = gson.fromJson(profileJson, TreinadorProfileData::class.java)
                    _uiState.value = PerfilUiState.SuccessTreinador(profileData)
                } else {
                    val profileData = gson.fromJson(profileJson, AlunoProfileData::class.java)
                    _uiState.value = PerfilUiState.SuccessAluno(profileData)
                }
            } catch (e: Exception) {
                android.util.Log.e("PerfilViewModel", "Erro ao processar JSON: ${e.message}")
                _uiState.value = PerfilUiState.Error("Falha ao processar dados do perfil")
            }
        }
    }

    /**
     * Atualiza o perfil do Aluno com suporte a foto opcional.
     */
    fun atualizarAluno(
        context: Context,
        id: String,
        nome: String,
        dataNascimento: String,
        sexo: String,
        peso: Double?,
        altura: Double?,
        fotoUri: Uri? = null,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isUpdating.value = true
            try {
                val json = JSONObject().apply {
                    put("nome", nome)
                    put("data_nascimento", dataNascimento)
                    put("sexo", sexo)
                    put("peso_atual_kg", peso)
                    put("altura_m", altura)
                }

                val requestBody = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
                val fotoPart = fotoUri?.let { FileUtils.createMultipartBody(context, it, "foto") }

                val response = RetrofitClient.profileApi.updateAlunoProfile(id, requestBody, fotoPart)
                if (response.success) {
                    _uiState.value = PerfilUiState.SuccessAluno(response.data)
                    onSuccess()
                }
            } catch (e: Exception) {
                android.util.Log.e("PerfilViewModel", "Erro ao atualizar aluno: ${e.message}")
            } finally {
                _isUpdating.value = false
            }
        }
    }

    /**
     * Atualiza o perfil do Treinador com suporte a foto opcional.
     */
    fun atualizarTreinador(
        context: Context,
        id: String,
        nome: String,
        cref: String,
        graduacao: String,
        especializacao: String,
        fotoUri: Uri? = null,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isUpdating.value = true
            try {
                val json = JSONObject().apply {
                    put("nome", nome)
                    put("cref", cref)
                    put("graduacao", graduacao)
                    put("especializacao", especializacao)
                }

                val requestBody = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
                val fotoPart = fotoUri?.let { FileUtils.createMultipartBody(context, it, "foto") }

                val response = RetrofitClient.profileApi.updateTreinadorProfile(id, requestBody, fotoPart)
                if (response.success) {
                    _uiState.value = PerfilUiState.SuccessTreinador(response.data)
                    onSuccess()
                }
            } catch (e: Exception) {
                android.util.Log.e("PerfilViewModel", "Erro ao atualizar treinador: ${e.message}")
            } finally {
                _isUpdating.value = false
            }
        }
    }
}

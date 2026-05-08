package dev.fslab.academia.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.fslab.academia.model.LoginRequest
import dev.fslab.academia.model.User
import dev.fslab.academia.model.toUser
import dev.fslab.academia.network.CookieManager
import dev.fslab.academia.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException

sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // ── Verificar sessão existente (auto-login ao abrir o app) ──────
    fun checkSession() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = RetrofitClient.authApi.getSession()
                val user = fetchUser(response.user)
                if (user != null) {
                    _currentUser.value = user
                    _authState.value = AuthState.Success(user)
                } else {
                    _currentUser.value = null
                    _authState.value = AuthState.Idle
                }
            } catch (_: HttpException) {
                _currentUser.value = null
                _authState.value = AuthState.Idle
            } catch (_: Exception) {
                _currentUser.value = null
                _authState.value = AuthState.Idle
            }
        }
    }

    // ── Login ───────────────────────────────────────────────────────
    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = RetrofitClient.authApi.login(
                    LoginRequest(email = email.trim(), password = password)
                )

                val user = fetchUser(response.user)
                if (user != null) {
                    _currentUser.value = user
                    _authState.value = AuthState.Success(user)
                } else {
                    _authState.value = AuthState.Error("Nao foi possivel obter os dados do usuario")
                }
            } catch (e: HttpException) {
                val apiMessage = e.response()?.errorBody()?.string()?.let(::extractApiErrorMessage)
                _authState.value = AuthState.Error(apiMessage ?: mapHttpError(e.code()))
            } catch (_: Exception) {
                _authState.value = AuthState.Error("Sem conexao com a internet")
            }
        }
    }


    // ── Logout ─────────────────────────────────────────────────────
    fun logout() {
        viewModelScope.launch {
            try {
                RetrofitClient.authApi.logout()
            } catch (_: Exception) {
                // Ignora erro de rede no logout — limpa localmente de qualquer forma
            } finally {
                CookieManager.clearCookies()      // Remove cookie do dispositivo
                _currentUser.value = null
                _authState.value = AuthState.Idle
            }
        }
    }

    private fun mapHttpError(code: Int): String = when (code) {
        400 -> "Dados invalidos. Verifique email e senha."
        401 -> "Email ou senha incorretos"
        403 -> "Acesso negado"
        429 -> "Muitas tentativas. Aguarde um momento."
        in 500..599 -> "Erro no servidor. Tente novamente mais tarde."
        else -> "Falha ao autenticar. Codigo HTTP: $code"
    }

    private fun extractApiErrorMessage(rawBody: String): String? {
        return runCatching {
            val json = JSONObject(rawBody)
            when {
                json.has("message") -> json.getString("message")
                json.has("error") -> json.getString("error")
                else -> null
            }
        }.getOrNull()?.takeIf { it.isNotBlank() }
    }

    private suspend fun fetchUser(fallback: dev.fslab.academia.model.UserData?): User? {
        val profile = runCatching { RetrofitClient.authApi.getProfile() }.getOrNull()
        val userData = profile?.data ?: fallback
        return userData?.toUser()
    }
}
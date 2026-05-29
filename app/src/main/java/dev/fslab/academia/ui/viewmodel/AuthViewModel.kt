package dev.fslab.academia.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import dev.fslab.academia.model.LoginRequest
import dev.fslab.academia.model.SocialLoginRequest
import dev.fslab.academia.model.SocialIdToken
import dev.fslab.academia.model.FcmTokenRequest
import dev.fslab.academia.model.User
import dev.fslab.academia.model.toUser
import dev.fslab.academia.network.CookieManager
import dev.fslab.academia.network.GoogleSignInHelper
import dev.fslab.academia.network.RetrofitClient
import dev.fslab.academia.network.SessionStore
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
                SessionStore.setToken(response.session?.token)
                val user = fetchUser(response.user)
                if (user != null) {
                    _currentUser.value = user
                    _authState.value = AuthState.Success(user)
                    enviarFcmTokenAposLogin()
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

                SessionStore.setToken(response.session?.token)

                val user = fetchUser(response.user)
                if (user != null) {
                    _currentUser.value = user
                    _authState.value = AuthState.Success(user)
                    enviarFcmTokenAposLogin()
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

    // ── Login com Google ────────────────────────────────────────────
    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = RetrofitClient.authApi.signInWithSocial(
                    SocialLoginRequest(
                        provider = "google",
                        callbackUrl = "/",
                        idToken = SocialIdToken(token = idToken)
                    )
                )

                SessionStore.setToken(response.session?.token)

                val user = fetchUser(response.user)
                if (user != null) {
                    _currentUser.value = user
                    _authState.value = AuthState.Success(user)
                    enviarFcmTokenAposLogin()
                } else {
                    _authState.value = AuthState.Error("Não foi possível autenticar com Google")
                }
            } catch (e: HttpException) {
                val msg = e.response()?.errorBody()?.string()?.let(::extractApiErrorMessage)
                _authState.value = AuthState.Error(msg ?: "Erro ao autenticar com Google")
            } catch (_: Exception) {
                _authState.value = AuthState.Error("Sem conexão com a internet")
            }
        }
    }


    // ── Logout ─────────────────────────────────────────────────────
    fun logout(context: Context? = null) {
        viewModelScope.launch {
            try {
                // 1. Limpar FCM token no servidor antes de deslogar
                try {
                    RetrofitClient.authApi.updateFcmToken(FcmTokenRequest(""))
                } catch (_: Exception) {}

                // 2. Chamar logout no BetterAuth (invalida sessão no servidor)
                RetrofitClient.authApi.logout()
            } catch (_: Exception) {
                // Ignora erro de rede no logout — limpa localmente de qualquer forma
            } finally {
                // 3. Limpar cookie e sessão local
                CookieManager.clearCookies()
                SessionStore.clear()

                // 4. Revogar Google Sign-In se context disponível
                context?.let { GoogleSignInHelper.signOut(it) }

                _currentUser.value = null
                _authState.value = AuthState.Idle
            }
        }
    }

    private fun enviarFcmTokenAposLogin() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            viewModelScope.launch {
                try {
                    RetrofitClient.authApi.updateFcmToken(FcmTokenRequest(token))
                } catch (_: Exception) { /* silencia — token será atualizado no próximo onNewToken */ }
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
        // Tenta obter o perfil detalhado que contém o 'type_usuario_autenticado'
        // Adicionando um pequeno delay ou retry se necessário para garantir que o cookie/token foi processado
        val profile = runCatching { RetrofitClient.authApi.getProfile() }.getOrNull()
        val userData = profile?.data ?: fallback
        return userData?.toUser()
    }
}

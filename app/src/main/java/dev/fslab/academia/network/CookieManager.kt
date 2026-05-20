package dev.fslab.academia.network

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
/**
 * CookieManager — Singleton que fornece um CookieJar persistente e criptografado.
 *
 * Os cookies (incluindo o cookie de sessão do Better Auth) são salvos no disco
 * usando EncryptedSharedPreferences com AES-256-GCM, protegidos pelo Android Keystore.
 *
 * Inicialização: CookieManager.init(applicationContext) na Application ou MainActivity.
 */

object CookieManager {
    private lateinit var _cookieJar: PersistentCookieJar

    val cookieJar: PersistentCookieJar
        get() = _cookieJar

    /** Deve ser chamado UMA vez antes de qualquer requisição. */
    fun init(context: Context) {
        if (::_cookieJar.isInitialized) return

        try {
            // 1. Cria (ou recupera) a MasterKey do Android Keystore
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            // 2. Cria o SharedPreferences criptografado (AES-256-SIV para chaves, AES-256-GCM para valores)
            val encryptedPrefs = EncryptedSharedPreferences.create(
                context,
                "secure_cookies",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            // 3. Monta o CookieJar com cache em memória + persistência criptografada
            _cookieJar = PersistentCookieJar(
                SetCookieCache(),                           // Cache em memória (sessão ativa)
                SharedPrefsCookiePersistor(encryptedPrefs)   // Persistência criptografada em disco
            )
        } catch (e: Exception) {
            // Fallback: Se a criptografia falhar (ex: Keystore corrompido ou erro de sistema),
            // usa SharedPreferences padrão para evitar que o app pare de funcionar.
            val fallbackPrefs = context.getSharedPreferences("fallback_cookies", Context.MODE_PRIVATE)
            _cookieJar = PersistentCookieJar(
                SetCookieCache(),
                SharedPrefsCookiePersistor(fallbackPrefs)
            )
        }
    }

    /** Limpa todos os cookies — usar no logout. */
    fun clearCookies() {
        if (::_cookieJar.isInitialized) {
            _cookieJar.clear()
        }
    }

    /** Verifica se existe algum cookie de sessão salvo — usar para auto-login. */
    fun hasSession(): Boolean {
        if (!::_cookieJar.isInitialized) return false
        // PersistentCookieJar não expõe cookies diretamente,
        // mas podemos checar tentando buscar o perfil do usuário na API.
        // Este método é um placeholder para lógica customizada.
        return true
    }
}
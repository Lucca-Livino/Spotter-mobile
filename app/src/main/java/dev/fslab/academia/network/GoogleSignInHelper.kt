package dev.fslab.academia.network

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

object GoogleSignInHelper {

    /**
     * Web Client ID gerado pelo Firebase Console (seção OAuth 2.0 → tipo "Web").
     * TODO: Substituir pelo ID real do projeto no Firebase Console.
     */
    private const val WEB_CLIENT_ID = "772560091614-3c1dder09ilpjg0pf53hqahck3n2brrj.apps.googleusercontent.com"

    private var client: GoogleSignInClient? = null

    fun getClient(context: Context): GoogleSignInClient {
        if (client != null) return client!!
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(WEB_CLIENT_ID)
            .requestEmail()
            .build()
        client = GoogleSignIn.getClient(context, gso)
        return client!!
    }

    fun getSignInIntent(context: Context): Intent =
        getClient(context).signInIntent

    /**
     * Extrai o ID Token do resultado do intent retornado pela Activity de Sign-In.
     * Retorna null se o usuário cancelou ou ocorreu erro.
     */
    fun getIdTokenFromResult(data: Intent?): String? {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            account.idToken
        } catch (e: ApiException) {
            null
        }
    }

    /** Revoga acesso Google no dispositivo — chamar no logout. */
    fun signOut(context: Context) {
        getClient(context).signOut()
        getClient(context).revokeAccess()
    }
}

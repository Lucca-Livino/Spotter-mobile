package dev.fslab.academia.network

object SessionStore {
    @Volatile
    private var token: String? = null

    fun setToken(value: String?) {
        token = value
    }

    fun getToken(): String? = token

    fun clear() {
        token = null
    }
}

package dev.fslab.academia.network

import dev.fslab.academia.model.AparelhoListResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface AparelhoApi {
    @GET("aparelhos")
    suspend fun listar(
        @Query("page") page: Int = 1,
        @Query("limite") limite: Int = 100,
        @Query("nome") nome: String? = null,
        @Query("ordem") ordem: String? = "nome_asc"
    ): AparelhoListResponse
}

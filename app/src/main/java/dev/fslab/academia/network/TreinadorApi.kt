package dev.fslab.academia.network

import dev.fslab.academia.model.AlunoListResponse
import dev.fslab.academia.model.TreinadorListResponse
import dev.fslab.academia.model.TreinadorSingleResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TreinadorApi {
    @GET("treinadores/me/alunos")
    suspend fun listarAlunosVinculados(
        @Query("page") page: Int = 1,
        @Query("limite") limite: Int = 50
    ): AlunoListResponse

    @GET("treinadores")
    suspend fun getAllTreinadores(
        @Query("page") page: Int = 1,
        @Query("limite") limite: Int = 50
    ): TreinadorListResponse

    @GET("treinadores/{id}")
    suspend fun getTreinadorById(@Path("id") id: String): TreinadorSingleResponse
}

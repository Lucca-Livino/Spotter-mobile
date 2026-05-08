package dev.fslab.academia.network

import dev.fslab.academia.model.AlunoListResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface TreinadorApi {
    @GET("treinadores/me/alunos")
    suspend fun listarAlunosVinculados(
        @Query("page") page: Int = 1,
        @Query("limite") limite: Int = 50
    ): AlunoListResponse
}

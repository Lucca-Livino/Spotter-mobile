package dev.fslab.academia.network

import dev.fslab.academia.model.MusculoListResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface MusculoApi {
    @GET("musculos")
    suspend fun listar(
        @Query("page") page: Int = 1,
        @Query("limite") limite: Int = 100,
        @Query("nome") nome: String? = null,
        @Query("grupo_muscular") grupoMuscular: String? = null,
        @Query("ordem") ordem: String? = "nome_asc",
        @Query("incluir_contagem_grupo") incluirContagemGrupo: Boolean? = null
    ): MusculoListResponse
}

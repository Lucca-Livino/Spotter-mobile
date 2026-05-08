package dev.fslab.academia.network

import dev.fslab.academia.model.EstatisticasResponse
import dev.fslab.academia.model.ExerciciosFrequentesResponse
import dev.fslab.academia.model.GruposMusculareResponse
import dev.fslab.academia.model.ProgressaoResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface HistoricoApi {

    @GET("historico/estatisticas")
    suspend fun getEstatisticas(
        @Query("data_inicio") dataInicio: String? = null,
        @Query("data_fim") dataFim: String? = null
    ): Response<EstatisticasResponse>

    @GET("historico/grupos-musculares")
    suspend fun getGruposMusculares(
        @Query("data_inicio") dataInicio: String? = null,
        @Query("data_fim") dataFim: String? = null
    ): Response<GruposMusculareResponse>

    @GET("historico/exercicios-frequentes")
    suspend fun getExerciciosFrequentes(
        @Query("data_inicio") dataInicio: String? = null,
        @Query("data_fim") dataFim: String? = null,
        @Query("limite") limite: Int = 10
    ): Response<ExerciciosFrequentesResponse>

    @GET("historico/progressao/{exercicioId}")
    suspend fun getProgressao(
        @Path("exercicioId") exercicioId: String,
        @Query("data_inicio") dataInicio: String? = null,
        @Query("data_fim") dataFim: String? = null,
        @Query("limite") limite: Int = 50
    ): Response<ProgressaoResponse>
}

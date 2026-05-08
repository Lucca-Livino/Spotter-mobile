package dev.fslab.academia.network

import dev.fslab.academia.model.SessaoData
import dev.fslab.academia.model.SessaoExercicioUpdateRequest
import dev.fslab.academia.model.SessaoResponse
import dev.fslab.academia.model.SessaoResumoData
import dev.fslab.academia.model.SessaoResumoResponse
import dev.fslab.academia.model.SessaoSeriesUpdateRequest
import dev.fslab.academia.model.SessaoListResponse
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface SessaoApi {
    @POST("sessoes")
    suspend fun iniciar(@Body body: RequestBody): Response<SessaoResponse>

    @GET("sessoes/em-andamento")
    suspend fun getEmAndamento(): Response<SessaoResponse>

    @GET("sessoes/{id}")
    suspend fun getById(@Path("id") id: String): Response<SessaoResponse>

    @GET("sessoes/{id}/resumo")
    suspend fun getResumo(@Path("id") id: String): Response<SessaoResumoResponse>

    @PUT("sessoes/{id}/exercicios/{exercicioId}/series")
    suspend fun updateSeries(
        @Path("id") sessaoId: String,
        @Path("exercicioId") sessaoExercicioId: String,
        @Body body: SessaoSeriesUpdateRequest
    ): Response<SessaoResponse>

    @PATCH("sessoes/{id}/exercicios/{exercicioId}")
    suspend fun updateExercicio(
        @Path("id") sessaoId: String,
        @Path("exercicioId") sessaoExercicioId: String,
        @Body body: SessaoExercicioUpdateRequest
    ): Response<SessaoResponse>

    @POST("sessoes/{id}/finalizar")
    suspend fun finalizar(@Path("id") id: String): Response<SessaoResponse>

    @POST("sessoes/{id}/cancelar")
    suspend fun cancelar(@Path("id") id: String): Response<SessaoResponse>

    @GET("sessoes")
    suspend fun listSessoes(
        @Query("page") page: Int = 1,
        @Query("limite") limite: Int = 10,
        @Query("status") status: String? = null,
        @Query("data_inicio") dataInicio: String? = null,
        @Query("data_fim") dataFim: String? = null,
        @Query("ordem_data_inicio") ordemDataInicio: String = "desc"
    ): Response<SessaoListResponse>
}

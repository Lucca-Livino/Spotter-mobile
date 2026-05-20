package dev.fslab.academia.network

import dev.fslab.academia.model.ConversaCriarRequest
import dev.fslab.academia.model.ConversaDetailResponse
import dev.fslab.academia.model.ConversaListResponse
import dev.fslab.academia.model.EnviarMensagemRequest
import dev.fslab.academia.model.MensagemDetailResponse
import dev.fslab.academia.model.MensagemLidasResponse
import dev.fslab.academia.model.MensagemListResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ConversaApi {
    @GET("conversas")
    suspend fun listarConversas(
        @Query("page") page: Int = 1,
        @Query("limite") limite: Int = 20
    ): ConversaListResponse

    @POST("conversas")
    suspend fun iniciarOuBuscar(
        @Body request: ConversaCriarRequest = ConversaCriarRequest()
    ): ConversaDetailResponse

    @GET("conversas/{id}")
    suspend fun obterPorId(
        @Path("id") conversaId: String
    ): ConversaDetailResponse

    @GET("conversas/{conversaId}/mensagens")
    suspend fun listarMensagens(
        @Path("conversaId") conversaId: String,
        @Query("page") page: Int = 1,
        @Query("limite") limite: Int = 50
    ): MensagemListResponse

    @POST("conversas/{conversaId}/mensagens")
    suspend fun enviarMensagem(
        @Path("conversaId") conversaId: String,
        @Body request: EnviarMensagemRequest
    ): MensagemDetailResponse

    @PATCH("conversas/{conversaId}/mensagens/lidas")
    suspend fun marcarComoLidas(
        @Path("conversaId") conversaId: String
    ): MensagemLidasResponse
}

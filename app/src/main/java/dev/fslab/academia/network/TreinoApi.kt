package dev.fslab.academia.network

import dev.fslab.academia.model.TreinoDeleteResponse
import dev.fslab.academia.model.TreinoDetailResponse
import dev.fslab.academia.model.TreinoListResponse
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TreinoApi {
    @GET("treinos")
    suspend fun listar(
        @Query("page") page: Int = 1,
        @Query("limite") limite: Int = 20,
        @Query("nome") nome: String? = null,
        @Query("usuario_id") usuarioId: String? = null,
        @Query("treinador_id") treinadorId: String? = null,
        @Query("dias_semana") diasSemana: String? = null,
        @Query("ordem_data_criacao") ordemDataCriacao: String = "desc",
        @Query("ordem_treino") ordemTreino: String? = null,
        @Query("incluir_exercicios") incluirExercicios: Boolean = false,
        @Query("somente_com_exercicios") somenteComExercicios: Boolean = false,
        @Query("incluir_inativos") incluirInativos: Boolean = false
    ): TreinoListResponse

    @GET("treinos/{id}")
    suspend fun buscarPorId(
        @Path("id") id: String,
        @Query("incluir_musculos") incluirMusculos: Boolean = true,
        @Query("incluir_aparelhos") incluirAparelhos: Boolean = true,
        @Query("apenas_ativos") apenasAtivos: Boolean = true,
        @Query("ordem_execucao") ordemExecucao: String = "asc"
    ): TreinoDetailResponse

    @POST("treinos")
    suspend fun criar(@Body body: RequestBody): TreinoDetailResponse

    @PATCH("treinos/{id}")
    suspend fun atualizar(
        @Path("id") id: String,
        @Body body: RequestBody
    ): TreinoDetailResponse

    @DELETE("treinos/{id}")
    suspend fun deletar(
        @Path("id") id: String,
        @Query("force") force: Boolean? = null
    ): TreinoDeleteResponse
}

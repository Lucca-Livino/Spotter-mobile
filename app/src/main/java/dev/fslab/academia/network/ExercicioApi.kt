package dev.fslab.academia.network

import dev.fslab.academia.model.ExercicioDeleteResponse
import dev.fslab.academia.model.ExercicioDetailResponse
import dev.fslab.academia.model.ExercicioListResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ExercicioApi {
    @GET("exercicios")
    suspend fun listar(
        @Query("page") page: Int = 1,
        @Query("limite") limite: Int = 20,
        @Query("nome") nome: String? = null,
        @Query("grupo_muscular") grupoMuscular: String? = null,
        @Query("tipo_ativacao") tipoAtivacao: String? = null,
        @Query("aluno_id") alunoId: String? = null,
        @Query("escopo") escopo: String? = null,
        @Query("em_uso") emUso: Boolean? = null,
        @Query("ordem_nome") ordemNome: String? = null,
        @Query("incluir_musculos") incluirMusculos: Boolean = true,
        @Query("incluir_aparelhos") incluirAparelhos: Boolean = true
    ): ExercicioListResponse

    @GET("exercicios/{id}")
    suspend fun buscarPorId(@Path("id") id: String): ExercicioDetailResponse

    @Multipart
    @POST("exercicios")
    suspend fun criar(
        @Part("data") data: RequestBody,
        @Part animacao: MultipartBody.Part? = null
    ): ExercicioDetailResponse

    @Multipart
    @PATCH("exercicios/{id}")
    suspend fun atualizar(
        @Path("id") id: String,
        @Part("data") data: RequestBody,
        @Part animacao: MultipartBody.Part? = null
    ): ExercicioDetailResponse

    @DELETE("exercicios/{id}")
    suspend fun deletar(
        @Path("id") id: String,
        @Query("soft") soft: Boolean? = null,
        @Query("force") force: Boolean? = null
    ): ExercicioDeleteResponse
}

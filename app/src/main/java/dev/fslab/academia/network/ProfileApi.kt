package dev.fslab.academia.network

import dev.fslab.academia.model.AlunoProfileResponse
import dev.fslab.academia.model.TreinadorProfileResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ProfileApi {

    // ── Aluno ─────────────────────────────────────────────────────────────────

    @GET("alunos/me")
    suspend fun getAlunoProfile(): AlunoProfileResponse

    @Multipart
    @POST("alunos")
    suspend fun createAlunoProfile(
        @Part("data") data: okhttp3.RequestBody,
        @Part foto: MultipartBody.Part? = null
    ): AlunoProfileResponse

    @Multipart
    @PATCH("alunos/{id}")
    suspend fun updateAlunoProfile(
        @Path("id") id: String,
        @Part("data") data: RequestBody,
        @Part foto: MultipartBody.Part? = null
    ): AlunoProfileResponse

    // ── Treinador ─────────────────────────────────────────────────────────────

    @GET("treinadores/me")
    suspend fun getTreinadorProfile(): TreinadorProfileResponse

    @Multipart
    @POST("treinadores")
    suspend fun createTreinadorProfile(
        @Part("data") data: okhttp3.RequestBody,
        @Part foto: MultipartBody.Part? = null
    ): TreinadorProfileResponse

    @Multipart
    @PATCH("treinadores/{id}")
    suspend fun updateTreinadorProfile(
        @Path("id") id: String,
        @Part("data") data: RequestBody,
        @Part foto: MultipartBody.Part? = null
    ): TreinadorProfileResponse
}

package dev.fslab.academia.network

import dev.fslab.academia.model.AcademiaListResponse
import retrofit2.http.GET

interface AcademiaApi {
    @GET("academia")
    suspend fun getAcademias(): AcademiaListResponse
}

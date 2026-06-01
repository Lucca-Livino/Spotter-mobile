package dev.fslab.academia.network

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    const val BASE_URL = "http://192.168.0.134:1350/api/"

    private val gson = GsonBuilder().setLenient().create()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .cookieJar(CookieManager.cookieJar)
        .addInterceptor(loggingInterceptor)
        .followRedirects(true)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val authApi: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }

    val profileApi: ProfileApi by lazy {
        retrofit.create(ProfileApi::class.java)
    }

    val exercicioApi: ExercicioApi by lazy {
        retrofit.create(ExercicioApi::class.java)
    }
    val treinoApi: TreinoApi by lazy { retrofit.create(TreinoApi::class.java) }
    val musculoApi: MusculoApi by lazy { retrofit.create(MusculoApi::class.java) }
    val aparelhoApi: AparelhoApi by lazy { retrofit.create(AparelhoApi::class.java) }
    val sessaoApi: SessaoApi by lazy { retrofit.create(SessaoApi::class.java) }
    val historicoApi: HistoricoApi by lazy { retrofit.create(HistoricoApi::class.java) }
    val academiaApi: AcademiaApi by lazy { retrofit.create(AcademiaApi::class.java) }
    val treinadorApi: TreinadorApi by lazy { retrofit.create(TreinadorApi::class.java) }
    val conversaApi: ConversaApi by lazy { retrofit.create(ConversaApi::class.java) }
}

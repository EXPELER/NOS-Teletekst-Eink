package nl.expeler.einkteletext.api

import nl.expeler.einkteletext.model.TeletextPage
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class TeletextRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val api = Retrofit.Builder()
        .baseUrl("https://teletekst-data.nos.nl/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(TeletextApiService::class.java)

    suspend fun getPage(page: String): Result<TeletextPage> = runCatching {
        api.getPage(page)
    }
}

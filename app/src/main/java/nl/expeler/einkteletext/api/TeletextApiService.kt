package nl.expeler.einkteletext.api

import nl.expeler.einkteletext.model.TeletextPage
import retrofit2.http.GET
import retrofit2.http.Path

interface TeletextApiService {
    @GET("json/{page}")
    suspend fun getPage(@Path("page") page: String): TeletextPage
}

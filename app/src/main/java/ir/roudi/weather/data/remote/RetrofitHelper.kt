package ir.roudi.weather.data.remote

import ir.roudi.weather.data.remote.service.CityService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitHelper {

    const val BASE_URL = "https://api.openweathermap.org/data/2.5/"
    const val API_URL = "${BASE_URL}"

    private val okHttpClient = OkHttpClient.Builder()
        .build()

    private val retrofit = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl(API_URL)
        .addConverterFactory(GsonConverterFactory.create(GsonHelper.gsonBuilder))
        .build()

    // Creating service is an expensive operation so we use `lazy`
    val cityService : CityService by lazy {
        retrofit.create(CityService::class.java)
    }

}
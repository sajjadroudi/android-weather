package ir.roudi.weather.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitHelper {

    const val BASE_URL = "https://api.openweathermap.org/"
    const val API_URL = "${BASE_URL}data/2.5/"

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(MainInterceptor())
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .callTimeout(10, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl(API_URL)
        .addConverterFactory(GsonConverterFactory.create(GsonHelper.gsonBuilder))
        .build()

    // Creating service is an expensive operation so we use `lazy`
    val service : Service by lazy {
        retrofit.create(Service::class.java)
    }

}
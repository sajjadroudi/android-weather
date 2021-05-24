package ir.roudi.weather.data.remote

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.roudi.weather.WeatherApp
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class MainInterceptor @Inject constructor(
    @ApplicationContext private val context: Context
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val newUrl = request.url()
                .newBuilder()
                .addQueryParameter("appid", (context as WeatherApp).apiKey)
                .addQueryParameter("units", "metric")
                .build()

        val newRequest = request.newBuilder()
                .url(newUrl)
                .build()

        return chain.proceed(newRequest)
    }

}
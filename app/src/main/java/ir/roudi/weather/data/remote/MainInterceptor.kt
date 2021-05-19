package ir.roudi.weather.data.remote

import ir.roudi.weather.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class MainInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val newUrl = request.url()
                .newBuilder()
                .addQueryParameter("appid", BuildConfig.API_KEY)
                .addQueryParameter("units", "metric")
                .build()

        val newRequest = request.newBuilder()
                .url(newUrl)
                .build()

        return chain.proceed(newRequest)
    }

}
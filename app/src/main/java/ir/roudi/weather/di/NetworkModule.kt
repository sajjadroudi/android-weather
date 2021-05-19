package ir.roudi.weather.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ir.roudi.weather.data.remote.GsonHelper
import ir.roudi.weather.data.remote.MainInterceptor
import ir.roudi.weather.data.remote.RetrofitHelper
import ir.roudi.weather.data.remote.Service
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // Creating service is an expensive operation so we use @Singleton
    @Provides
    @Singleton
    fun provideService(interceptor: MainInterceptor) : Service {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .callTimeout(10, TimeUnit.SECONDS)
            .build()

        val converterFactory = GsonConverterFactory.create(GsonHelper.gsonBuilder)

        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(RetrofitHelper.API_URL)
            .addConverterFactory(converterFactory)
            .build()
            .create(Service::class.java)
    }

}
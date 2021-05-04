package ir.roudi.weather.data.remote.service

import ir.roudi.weather.BuildConfig
import ir.roudi.weather.data.remote.response.City
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface CityService {

    @GET("weather")
    suspend fun getCity(@Query("lat") lat: Double, @Query("lon") lon: Double, @Query("appid") apiKey: String = BuildConfig.API_KEY) : Response<City>

}
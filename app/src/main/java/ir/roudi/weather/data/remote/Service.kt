package ir.roudi.weather.data.remote

import ir.roudi.weather.data.remote.response.City
import ir.roudi.weather.data.remote.response.Weather
import retrofit2.http.GET
import retrofit2.http.Query

interface Service {

    @GET(ENDPOINT)
    suspend fun getCity(@Query("lat") lat: Double, @Query("lon") lon: Double) : City

    @GET(ENDPOINT)
    suspend fun findCity(@Query("q") name: String) : City?

    @GET(ENDPOINT)
    suspend fun getWeather(@Query("id") cityId: Int) : Weather

    companion object {
        const val ENDPOINT = "weather"
    }

}
package ir.roudi.weather.data

import ir.roudi.weather.data.local.db.entity.City
import ir.roudi.weather.data.remote.response.Coordinates
import java.lang.IllegalArgumentException
import ir.roudi.weather.data.local.db.entity.Weather as LocalWeather
import ir.roudi.weather.data.remote.response.Weather as RemoteWeather

import ir.roudi.weather.data.local.db.entity.City as LocalCity
import ir.roudi.weather.data.remote.response.City as RemoteCity

fun RemoteWeather.toLocalWeather(cityId: Int) : LocalWeather =  LocalWeather(
        cityId,
        main,
        description,
        iconId,
        temperature,
        pressure,
        humidityPercent,
        minTemperature,
        maxTemperature,
        windSpeed,
        cloudinessPercent,
        lastHourRainVolume,
        lastHourSnowVolume,
        time,
        sunrise,
        sunset
    )

fun List<RemoteWeather>.toLocalWeather(cities: List<City>) : List<LocalWeather> {
    if(size != cities.size)
        throw IllegalArgumentException("Size of two list must be equal.")

    val list = mutableListOf<LocalWeather>()

    for(i in 0..lastIndex) {
        val cityId = cities[i].cityId
        val remoteWeather = get(i)
        val localWeather = remoteWeather.toLocalWeather(cityId)
        list.add(localWeather)
    }

    return list
}

fun LocalWeather.toRemoteWeather() : RemoteWeather = RemoteWeather(
        main,
        description,
        iconId,
        temperature,
        pressure,
        humidityPercent,
        minTemperature,
        maxTemperature,
        windSpeed,
        cloudinessPercent,
        lastHourRainVolume,
        lastHourSnowVolume,
        time,
        sunrise,
        sunset
)

fun RemoteCity.toLocalCity() : LocalCity = LocalCity(
        id,
        name,
        countryCode,
        coordinates.longitude,
        coordinates.latitude
)

fun LocalCity.toRemoteCity() : RemoteCity = RemoteCity(
        cityId,
        name,
        countryCode,
        coordinates = Coordinates(longitude, latitude)
)
package ir.roudi.weather.data.remote.response

data class City(
    val id: Int,
    val name: String,
    val countryCode: String,
    val coordinates: Coordinates
)

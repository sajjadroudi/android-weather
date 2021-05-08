package ir.roudi.weather.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "city")
data class City(
    @ColumnInfo(name = "city_id") @PrimaryKey val cityId: Int,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "country_code") val countryCode: String,
    @ColumnInfo(name = "longitude") val longitude: Double,
    @ColumnInfo(name = "latitude") val latitude: Double
)

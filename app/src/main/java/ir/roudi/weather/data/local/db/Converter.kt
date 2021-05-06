package ir.roudi.weather.data.local.db

import androidx.room.TypeConverter
import java.util.*

class Converter {

    @TypeConverter
    fun fromTimestamp(value: Long) : Calendar {
        return Calendar.getInstance().apply { timeInMillis = value }
    }

    @TypeConverter
    fun calendarToTimestamp(calendar: Calendar) : Long {
        return calendar.timeInMillis
    }

}
package ir.roudi.weather.data.local.db

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.*

class ConverterTest {

    private val calendar = Calendar.getInstance().apply {
        set(Calendar.YEAR, 2001)
        set(Calendar.MONTH, Calendar.FEBRUARY)
        set(Calendar.DAY_OF_MONTH, 23)
    }

    @Test
    fun fromTimestamp() {
        assertThat(Converter().fromTimestamp(calendar.timeInMillis))
                .isEqualTo(calendar)
    }

    @Test
    fun calendarToTimestamp() {
        assertThat(Converter().calendarToTimestamp(calendar))
                .isEqualTo(calendar.timeInMillis)
    }

}
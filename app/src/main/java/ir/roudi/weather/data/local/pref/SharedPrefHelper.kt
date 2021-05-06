package ir.roudi.weather.data.local.pref

import android.content.Context
import android.content.SharedPreferences

class SharedPrefHelper(
        private val context: Context
) {

    private val pref : SharedPreferences
        get() = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)

    fun setInt(key: String, value: Int) {
        pref.edit()
            .putInt(key, value)
            .apply()
    }

    fun getInt(key: String, defValue: Int = 0) =
        pref.getInt(key, defValue)

    companion object {
        private const val PREF_FILE = "ir.roudi.weather.SHARED_PREF"
        const val SELECTED_CITY_ID = "selected_city_id"
    }

}
package ir.roudi.weather.data.local.pref

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

class DefaultSharedPrefHelper constructor(
        private val context: Context
) : SharedPrefHelper {

    private val pref : SharedPreferences
        get() = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)

    override fun setInt(key: String, value: Int) {
        pref.edit()
            .putInt(key, value)
            .apply()
    }

    override fun getInt(key: String, defValue: Int) =
        pref.getInt(key, defValue)

    companion object {
        private const val PREF_FILE = "ir.roudi.weather.SHARED_PREF"
        const val SELECTED_CITY_ID = "selected_city_id"
    }

}
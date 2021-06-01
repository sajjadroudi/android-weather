package ir.roudi.weather.data.local.pref

interface SharedPrefHelper {
    fun setInt(key: String, value: Int)
    fun getInt(key: String, defValue: Int = 0): Int
}
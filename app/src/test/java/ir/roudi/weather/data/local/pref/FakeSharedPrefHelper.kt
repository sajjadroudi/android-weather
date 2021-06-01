package ir.roudi.weather.data.local.pref

class FakeSharedPrefHelper : SharedPrefHelper {

    private val source = mutableMapOf<String, Any>()

    override fun setInt(key: String, value: Int) {
        source[key] = value
    }

    override fun getInt(key: String, defValue: Int): Int {
        return source[key] as? Int ?: defValue
    }

}
package ir.roudi.weather.utils

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.view.View
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import ir.roudi.weather.data.local.db.entity.Weather
import ir.roudi.weather.widget.WeatherAppWidgetProvider

fun Context.isInternetConnected(): Boolean {
    // TODO: `activeNetworkInfo` is deprecated in android 10, use `NetworkCallbacks` for apps with android 10 and higher
    val manager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork = manager.activeNetworkInfo
    val isConnected = activeNetwork?.isConnectedOrConnecting == true
    return isConnected
}

fun <T> LiveData<T>.observeOnce(observer: Observer<in T>) {
    observeForever(object : Observer<T> {
        override fun onChanged(data: T) {
            observer.onChanged(data)
            removeObserver(this)
        }
    })
}

fun Context.getBitmap(weather: Weather): Bitmap {
    val inputStream = assets.open( "img_${weather.iconId}.png")
    val drawable = Drawable.createFromStream(inputStream, null)
    return drawable.toBitmap()
}

fun Context.updateWidgets() {
    val widgetIds = AppWidgetManager.getInstance(this).getAppWidgetIds(
            ComponentName(this, WeatherAppWidgetProvider::class.java)
    )

    val intent = Intent(this, WeatherAppWidgetProvider::class.java).apply {
        action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
    }

    sendBroadcast(intent)
}

fun View.snackbar(message: String) {
    Snackbar.make(this, message, Snackbar.LENGTH_SHORT).show()
}
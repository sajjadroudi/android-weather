package ir.roudi.weather.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import ir.roudi.weather.R
import ir.roudi.weather.data.Repository
import ir.roudi.weather.data.local.db.AppDatabase
import ir.roudi.weather.data.local.pref.SharedPrefHelper
import ir.roudi.weather.data.remote.RetrofitHelper
import ir.roudi.weather.ui.MainActivity
import ir.roudi.weather.utils.getBitmap
import ir.roudi.weather.utils.observeOnce
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException

class WeatherAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        val db = AppDatabase.getInstance(context)
        val repository = Repository(
                db.cityDao,
                db.weatherDao,
                RetrofitHelper.service,
                SharedPrefHelper(context)
        )

        val cityId = repository.getInt(SharedPrefHelper.SELECTED_CITY_ID)
        if(cityId == 0) {
            appWidgetIds.forEach { appWidgetId ->
                val pendingIntent : PendingIntent = Intent(context, MainActivity::class.java)
                        .let { intent ->  PendingIntent.getActivity(context, 0, intent, 0) }

                val views: RemoteViews = RemoteViews(
                        context.packageName,
                        R.layout.widget_weather
                ).apply {
                    setOnClickPendingIntent(R.id.container, pendingIntent)
                    setViewVisibility(R.id.unknown_container, View.VISIBLE)
                    setViewVisibility(R.id.weather_container, View.GONE)
                }

                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
            return
        }

        GlobalScope.launch {
            try {
                repository.refreshWeather(cityId)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        val weather = repository.getWeather(cityId)
        weather.observeOnce { weather ->
            weather ?: return@observeOnce

            appWidgetIds.forEach { appWidgetId ->
                val pendingIntent : PendingIntent = Intent(context, MainActivity::class.java)
                        .let { intent ->  PendingIntent.getActivity(context, 0, intent, 0) }

                val views: RemoteViews = RemoteViews(
                        context.packageName,
                        R.layout.widget_weather
                ).apply {
                    setOnClickPendingIntent(R.id.container, pendingIntent)
                    setTextViewText(R.id.txt_temp, weather.temperature.toString())
                    setTextViewText(R.id.txt_weather, weather.main)
                    setImageViewBitmap(R.id.img_weather, context.getBitmap(weather))
                    setViewVisibility(R.id.unknown_container, View.GONE)
                    setViewVisibility(R.id.weather_container, View.VISIBLE)
                }

                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }

        val city = repository.getCity(cityId)
        city.observeOnce {
            appWidgetIds.forEach { appWidgetId ->
                val views = RemoteViews(
                        context.packageName,
                        R.layout.widget_weather
                )

                views.setTextViewText(R.id.txt_city_name, it.name)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }

    }

}
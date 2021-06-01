package ir.roudi.weather.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import dagger.hilt.android.AndroidEntryPoint
import ir.roudi.weather.R
import ir.roudi.weather.data.repository.Repository
import ir.roudi.weather.data.local.db.entity.City
import ir.roudi.weather.data.local.db.entity.Weather
import ir.roudi.weather.data.local.pref.DefaultSharedPrefHelper
import ir.roudi.weather.ui.MainActivity
import ir.roudi.weather.utils.getBitmap
import ir.roudi.weather.utils.observeOnce
import javax.inject.Inject

@AndroidEntryPoint
class WeatherAppWidgetProvider : AppWidgetProvider() {

    @Inject lateinit var repository: Repository

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        val cityId = repository.getInt(DefaultSharedPrefHelper.SELECTED_CITY_ID)
        if(cityId == 0) {
            handleNoCitySelected(appWidgetIds, context, appWidgetManager)
            return
        }

        repository.fetchWeather(cityId).observeOnce {
            updateWeather(it, appWidgetIds, context, appWidgetManager)
        }

        repository.getCity(cityId).observeOnce {
            updateCity(it, appWidgetIds, context, appWidgetManager)
        }

    }

    private fun updateWeather(
            weather: Weather?,
            appWidgetIds: IntArray,
            context: Context,
            appWidgetManager: AppWidgetManager
    ) {
        weather ?: return

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

    private fun updateCity(
        city: City,
        appWidgetIds: IntArray,
        context: Context,
        appWidgetManager: AppWidgetManager
    ) {
        appWidgetIds.forEach { appWidgetId ->
            val views = RemoteViews(
                    context.packageName,
                    R.layout.widget_weather
            )

            views.setTextViewText(R.id.txt_city_name, city.name)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    private fun handleNoCitySelected(
            appWidgetIds: IntArray,
            context: Context,
            appWidgetManager: AppWidgetManager
    ) {
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
    }

}
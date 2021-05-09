package ir.roudi.weather

import android.app.Application
import androidx.work.*
import ir.roudi.weather.work.RefreshDataWorker

class WeatherApp : Application() {

    fun enqueueSyncWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<RefreshDataWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(applicationContext).enqueue(request)
    }

}
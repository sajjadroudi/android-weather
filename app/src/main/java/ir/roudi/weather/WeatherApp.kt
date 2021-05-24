package ir.roudi.weather

import android.app.Application
import android.content.pm.PackageManager
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import dagger.hilt.android.HiltAndroidApp
import ir.roudi.weather.work.RefreshDataWorker
import javax.inject.Inject

@HiltAndroidApp
class WeatherApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    val apiKey : String by lazy {
        packageManager.getApplicationInfo(
            packageName,
            PackageManager.GET_META_DATA
        ).metaData["API_KEY"].toString()
    }

    fun enqueueSyncWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<RefreshDataWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(applicationContext).enqueue(request)
    }

    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

}
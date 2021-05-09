package ir.roudi.weather.ui

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import ir.roudi.weather.R
import ir.roudi.weather.WeatherApp
import ir.roudi.weather.data.Repository
import ir.roudi.weather.data.local.db.AppDatabase
import ir.roudi.weather.data.local.pref.SharedPrefHelper
import ir.roudi.weather.data.remote.RetrofitHelper

class MainActivity : AppCompatActivity() {

    private val viewModel by viewModels<MainViewModel> {
        val db = AppDatabase.getInstance(this)
        val repository = Repository(db.cityDao, db.weatherDao, RetrofitHelper.service, SharedPrefHelper(this))
        MainViewModel.Factory(repository)
    }

    private val navController: NavController by lazy {
        findNavController(R.id.nav_host_fragment)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<BottomNavigationView>(R.id.bottom_nav)
            .setupWithNavController(navController)

        if(isConnected()) {
            viewModel.refresh()
        } else {
            (application as WeatherApp).enqueueSyncWork()
        }
    }

    private fun isConnected(): Boolean {
        // TODO: `activeNetworkInfo` is deprecated in android 10, use `NetworkCallbacks` for apps with android 10 and higher
        val manager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = manager.activeNetworkInfo
        val isConnected = activeNetwork?.isConnectedOrConnecting == true
        return isConnected
    }

    companion object {
        const val TAG = "MainActivity"
    }
}
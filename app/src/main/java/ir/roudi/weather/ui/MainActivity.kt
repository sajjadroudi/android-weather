package ir.roudi.weather.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import ir.roudi.weather.R
import ir.roudi.weather.WeatherApp
import ir.roudi.weather.data.Repository
import ir.roudi.weather.data.local.db.AppDatabase
import ir.roudi.weather.data.local.pref.SharedPrefHelper
import ir.roudi.weather.data.remote.RetrofitHelper
import ir.roudi.weather.utils.isInternetConnected

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

        val swipeRefresh = findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)
        swipeRefresh.setOnRefreshListener {
            if(isInternetConnected()) {
                viewModel.refresh()
            } else {
                Toast.makeText(this, "No Internet", Toast.LENGTH_SHORT).show()
                swipeRefresh.isRefreshing = false
            }
        }

        viewModel.hasDataFetched.observe(this) {
            if(it == true)
                swipeRefresh.isRefreshing = false
        }

        if(isInternetConnected()) {
            viewModel.refresh()
        } else {
            (application as WeatherApp).enqueueSyncWork()
        }
    }
}
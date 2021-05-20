package ir.roudi.weather.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import ir.roudi.weather.R
import ir.roudi.weather.WeatherApp
import ir.roudi.weather.utils.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel : MainViewModel by viewModels()

    private val navController: NavController by lazy {
        findNavController(R.id.nav_host_fragment)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<BottomNavigationView>(R.id.bottom_nav)
            .setupWithNavController(navController)

        // When cities are loaded from database
        // if Internet is connected, try to refresh their weathers from network
        // otherwise delegate it to the time that the device connects to Internet.
        viewModel.cities.observeOnce {
            if(isInternetConnected()) {
                viewModel.refresh()
            } else {
                (application as WeatherApp).enqueueSyncWork()
            }
        }

        val swipeRefresh = findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)
        swipeRefresh.setOnRefreshListener {
            viewModel.refresh(true)
        }

        viewModel.actionShowRefresh.observe(this) { shouldRefresh ->
            shouldRefresh?.let { swipeRefresh.isRefreshing = it  }
        }

        viewModel.errorMessage.observe(this) {
            it?.getContentIfNotHandled()?.let { message ->
                swipeRefresh.snackbar("Error: $message")
            }
        }

        viewModel.shouldUpdateWidget.observe(this) {
            it?.getContentIfNotHandled()?.let { shouldUpdate ->
                if(shouldUpdate) updateWidgets()
            }
        }
    }
}
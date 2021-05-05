package ir.roudi.weather.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView
import ir.roudi.weather.R
import ir.roudi.weather.data.local.AppDatabase
import ir.roudi.weather.data.remote.RetrofitHelper
import ir.roudi.weather.data.Repository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocation : Location? = null
    private val locationPermission = if(DEBUG_MODE) Manifest.permission.ACCESS_FINE_LOCATION
    else Manifest.permission.ACCESS_COARSE_LOCATION

    private lateinit var navController: NavController
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        navController = findNavController(R.id.nav_host_fragment)

        bottomNav = findViewById(R.id.bottom_nav)
        bottomNav.setupWithNavController(navController)

    }

    override fun onStart() {
        super.onStart()
        if (isLocationPermissionGranted()) {
            getLastLocation()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(locationPermission), REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == REQUEST_CODE) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    currentLocation = location

                    Log.i(TAG, "getLastLocation: ${location?.latitude} ${location?.longitude}")

                    val database = AppDatabase.getInstance(this@MainActivity)

                    val repository = Repository(
                        database.cityDao,
                        database.weatherDao,
                        RetrofitHelper.service
                    )

                    repository.cities.observe(this@MainActivity, Observer {
                        if(it == null || it.isEmpty()) return@Observer
                        val city = it[0]
                        Log.i(TAG, "new city: $city")
                    })

                    GlobalScope.launch {
                        val city = RetrofitHelper.service.getCity(location?.latitude!!, location.longitude)
                        Log.i(TAG, "City: $city")

                        val weather = RetrofitHelper.service.getWeather(city.id)
                        Log.i(TAG, "Weather: $weather")

                        repository.insertCity(location.latitude, location.longitude)

                        repository.refresh()
                    }

                }.addOnFailureListener {
                    Log.i(TAG, "getLastLocation : failure due to ${it.message}")
                }.addOnCanceledListener {
                    Log.i(TAG, "getLastLocation : cancel")
                }
    }

    private fun isLocationPermissionGranted()
        = ActivityCompat.checkSelfPermission(this, locationPermission) == PackageManager.PERMISSION_GRANTED

    companion object {
        const val REQUEST_CODE = 1
        const val DEBUG_MODE = true
        const val TAG = "MainActivity"
    }
}
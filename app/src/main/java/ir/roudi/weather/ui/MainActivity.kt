package ir.roudi.weather.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import ir.roudi.weather.R
import ir.roudi.weather.data.remote.RetrofitHelper
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var txtHello : TextView
    private var currentLocation : Location? = null
    private val locationPermission = if(DEBUG_MODE) Manifest.permission.ACCESS_FINE_LOCATION
    else Manifest.permission.ACCESS_COARSE_LOCATION

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        txtHello = findViewById(R.id.txt_hello)
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

                    GlobalScope.launch {
                        val city = async { RetrofitHelper.cityService.getCity(location?.latitude!!, location.longitude) }
                        Log.i(TAG, "getLastLocation: ${city.await().body()}")
                    }

                    Log.i(TAG, "getLastLocation: ${location?.longitude} - ${location?.latitude}")
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
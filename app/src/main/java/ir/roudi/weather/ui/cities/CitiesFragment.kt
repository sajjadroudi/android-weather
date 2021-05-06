package ir.roudi.weather.ui.cities

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import ir.roudi.weather.data.Repository
import ir.roudi.weather.data.local.db.AppDatabase
import ir.roudi.weather.data.local.db.entity.City
import ir.roudi.weather.data.local.pref.SharedPrefHelper
import ir.roudi.weather.data.remote.RetrofitHelper
import ir.roudi.weather.databinding.FragmentCitiesBinding

class CitiesFragment : Fragment() {

    private val viewModel : CitiesViewModel by lazy {
        val db = AppDatabase.getInstance(requireContext())
        val repository = Repository(db.cityDao, db.weatherDao, RetrofitHelper.service, SharedPrefHelper(requireContext()))
        ViewModelProvider(this, CitiesViewModel.Factory(repository))
            .get(CitiesViewModel::class.java)
    }

    private val adapter : CitiesAdapter by lazy {
        CitiesAdapter(object : CitiesAdapter.AdapterCallback {
            override fun onClick(city: City) {
                viewModel.setSelectedCityId(city.cityId)
            }

            override fun onDelete(city: City) {
                viewModel.deleteCity(city)
            }
        })
    }

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(requireContext())
    }

    private lateinit var binding : FragmentCitiesBinding

    private val locationPermission = if(DEBUG_MODE) Manifest.permission.ACCESS_FINE_LOCATION
    else Manifest.permission.ACCESS_COARSE_LOCATION

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setupBinding(inflater, container)

        viewModel.actionAddNewCity.observe(viewLifecycleOwner) {
            if(it == null || it == false) return@observe
            insertLastLocation()
            viewModel.addNewCityCompleted()
        }

        viewModel.selectedCityId.observe(viewLifecycleOwner) {
            it ?: return@observe
            adapter.selectedCityId = it
            adapter.notifyDataSetChanged()
        }

        return binding.root
    }

    private fun setupBinding(inflater: LayoutInflater, container: ViewGroup?) {
        binding = FragmentCitiesBinding.inflate(inflater, container, false)
        binding.recyclerView.adapter = adapter
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
    }

    private fun isLocationPermissionGranted()
            = ActivityCompat.checkSelfPermission(requireContext(), locationPermission) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    private fun insertLastLocation() {
        if (isLocationPermissionGranted()) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location ?: return@addOnSuccessListener
                    viewModel.insertCity(location.latitude, location.longitude)
                }.addOnFailureListener {
                    Log.i(TAG, "getLastLocation : failure due to ${it.message}")
                }.addOnCanceledListener {
                    Log.i(TAG, "getLastLocation : cancel")
                }
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(locationPermission), REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == REQUEST_CODE) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                insertLastLocation()
            }
        }
    }

    companion object {
        const val REQUEST_CODE = 1
        const val DEBUG_MODE = true
        const val TAG = "CitiesFragment"
    }

}
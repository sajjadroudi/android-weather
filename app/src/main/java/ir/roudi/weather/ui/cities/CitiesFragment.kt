package ir.roudi.weather.ui.cities

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import ir.roudi.weather.data.Repository
import ir.roudi.weather.data.local.db.AppDatabase
import ir.roudi.weather.data.local.db.entity.City
import ir.roudi.weather.data.local.pref.SharedPrefHelper
import ir.roudi.weather.data.remote.RetrofitHelper
import ir.roudi.weather.databinding.DialogAddCityBinding
import ir.roudi.weather.databinding.DialogEditCityBinding
import ir.roudi.weather.databinding.DialogFindCityBinding
import ir.roudi.weather.databinding.FragmentCitiesBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ir.roudi.weather.data.remote.response.City as RemoteCity


class CitiesFragment : Fragment() {

    private val viewModel by viewModels<CitiesViewModel> {
        val db = AppDatabase.getInstance(requireContext())
        val repository = Repository(db.cityDao, db.weatherDao, RetrofitHelper.service, SharedPrefHelper(requireContext()))
        CitiesViewModel.Factory(repository)
    }

    private val adapter : CitiesAdapter by lazy {
        CitiesAdapter(object : CitiesAdapter.AdapterCallback {
            override fun onClick(city: City) {
                viewModel.setSelectedCityId(city.cityId)
            }

            override fun onDelete(city: City) {
                showConfirmDialog {
                    viewModel.deleteCity(city)
                }
            }

            override fun onChange(city: City) {
                showEditDialog(city) { newCity ->
                    viewModel.updateCity(newCity)

                    val position = adapter.currentList.indexOfFirst { it.cityId == city.cityId }
                    if(position >= 0) adapter.notifyItemChanged(position)
                }
            }

            private fun showConfirmDialog(yesListener: () -> Unit) {
                AlertDialog.Builder(context)
                        .setPositiveButton("Delete") { _, _ ->
                            yesListener.invoke()
                        }
                        .setTitle("Delete City")
                        .setMessage("Are you sure?")
                        .setNegativeButton(android.R.string.cancel, null)
                        .create()
                        .show()
            }

            private fun showEditDialog(city: City, yesListener: (City) -> Unit) {
                val dialogBinding = DialogEditCityBinding.inflate(
                        layoutInflater, binding.root as ViewGroup, false
                )

                dialogBinding.edtName.apply {
                    setText(city.name)
                    selectAll()
                }

                AlertDialog.Builder(context)
                        .setTitle("Rename city")
                        .setPositiveButton("Rename") { _, _ ->
                            city.name = dialogBinding.edtName.text.toString().trim()
                            yesListener.invoke(city)
                        }
                        .setNegativeButton(android.R.string.cancel, null)
                        .setView(dialogBinding.root)
                        .create()
                        .show()
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

        viewModel.cities.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        viewModel.actionAddNewCity.observe(viewLifecycleOwner) {
            if(it == null || it == false) return@observe

            showAddCityDialog { useCurrentLocation ->
                if(useCurrentLocation) saveLastLocationAsCity()
                else showFindCityByNameDialog(
                        findListener = { name ->
                            viewModel.findCity(name)
                        },
                        saveListener = { remoteCity ->
                            viewModel.insertCity(remoteCity)
                        }
                )
            }

            viewModel.addNewCityCompleted()
        }

        viewModel.selectedCityId.observe(viewLifecycleOwner) {
            it ?: return@observe
            adapter.selectedCityId = it
            adapter.notifySelectedCityChanged(viewModel.oldSelectedCityId, it)
        }

        return binding.root
    }

    private fun setupBinding(inflater: LayoutInflater, container: ViewGroup?) {
        binding = FragmentCitiesBinding.inflate(inflater, container, false)
        binding.recyclerView.adapter = adapter
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
    }

    private fun showAddCityDialog(listener: (useCurrentLocation: Boolean) -> Unit) {
        val dialogBinding = DialogAddCityBinding.inflate(
                layoutInflater, binding.root as ViewGroup, false
        )

        val dialog = AlertDialog.Builder(context)
                .setNegativeButton(android.R.string.cancel, null)
                .setView(dialogBinding.root)
                .create()

        dialog.show()

        dialogBinding.btnCurrentLocation.setOnClickListener {
            listener.invoke(true)
            dialog.dismiss()
        }

        dialogBinding.btnSearch.setOnClickListener {
            listener.invoke(false)
            dialog.dismiss()
        }
    }

    private fun showFindCityByNameDialog(
            findListener: suspend (name: String) -> RemoteCity?,
            saveListener: (RemoteCity) -> Unit
    ) {
        val dialogBinding = DialogFindCityBinding.inflate(
                layoutInflater, binding.root as ViewGroup, false
        )

        var remoteCity : RemoteCity? = null
        var job : Job? = null

        dialogBinding.edtName.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                job?.cancel()
                job = lifecycleScope.launch {
                    try {
                        remoteCity = findListener.invoke(s?.toString() ?: "")

                        dialogBinding.txtName.apply {
                            text = remoteCity!!.name
                            setTextColor(Color.BLACK)
                        }
                    } catch (e: Exception) {
                        remoteCity = null
                        dialogBinding.txtName.apply {
                            text = "Not Found!"
                            setTextColor(Color.RED)
                        }
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })


        AlertDialog.Builder(context)
                .setPositiveButton("Add") { _, _->
                    if(remoteCity == null) {
                        Toast.makeText(context, "No city found!", Toast.LENGTH_SHORT).show()
                    } else {
                        saveListener.invoke(remoteCity!!)
                    }
                }
                .setNegativeButton(android.R.string.cancel, null)
                .setView(dialogBinding.root)
                .create()
                .show()
    }

    private fun isLocationPermissionGranted()
            = ActivityCompat.checkSelfPermission(requireContext(), locationPermission) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    private fun saveLastLocationAsCity() {
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
                saveLastLocationAsCity()
            }
        }
    }

    companion object {
        const val REQUEST_CODE = 1
        const val DEBUG_MODE = true
        const val TAG = "CitiesFragment"
    }

}
package ir.roudi.weather.ui.cities

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.CompositePermissionListener
import com.karumi.dexter.listener.single.PermissionListener
import com.karumi.dexter.listener.single.SnackbarOnDeniedPermissionListener
import dagger.hilt.android.AndroidEntryPoint
import ir.roudi.weather.data.local.db.entity.City
import ir.roudi.weather.databinding.DialogAddCityBinding
import ir.roudi.weather.databinding.DialogEditCityBinding
import ir.roudi.weather.databinding.DialogFindCityBinding
import ir.roudi.weather.databinding.FragmentCitiesBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import ir.roudi.weather.data.Result
import ir.roudi.weather.utils.snackbar
import ir.roudi.weather.utils.updateWidgets
import kotlinx.coroutines.flow.collect
import ir.roudi.weather.data.remote.response.City as RemoteCity

@AndroidEntryPoint
class CitiesFragment : Fragment() {

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(requireContext())
    }

    private lateinit var binding : FragmentCitiesBinding

    private val locationPermission = Manifest.permission.ACCESS_COARSE_LOCATION

    private val viewModel : CitiesViewModel by viewModels()

    private val adapter : CitiesAdapter by lazy {
        CitiesAdapter(object : CitiesAdapter.ItemCallback {
            override fun onClick(city: City) {
                viewModel.setSelectedCityId(city.cityId)
            }

            override fun onDelete(city: City) {
                showConfirmDialog {
                    viewModel.deleteCity(city)
                }
            }

            override fun onEdit(city: City) {
                showEditDialog(city) { newCity ->
                    viewModel.updateCity(newCity)

                    val index = adapter.currentList.indexOfFirst { it.cityId == city.cityId }
                    if(index >= 0) adapter.notifyItemChanged(index)
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setupBinding(inflater, container)

        viewModel.error.observe(viewLifecycleOwner) {
            it?.getContentIfNotHandled()?.let { message ->
                binding.root.snackbar("Error: $message")
            }
        }

        viewModel.actionAddNewCity.observe(viewLifecycleOwner) {
            it?.getContentIfNotHandled()?.let { shouldAddNewCity ->
                if(shouldAddNewCity) {
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
                }
            }
        }

        viewModel.selectedCityId.observe(viewLifecycleOwner) {
            it ?: return@observe
            adapter.selectedCityId = it
            adapter.notifySelectedCityChanged(viewModel.oldSelectedCityId, it)
        }

        viewModel.shouldUpdateWidget.observe(viewLifecycleOwner) {
            it?.getContentIfNotHandled()?.let { shouldUpdate ->
                if(shouldUpdate) requireActivity().updateWidgets()
            }
        }

        viewModel.message.observe(viewLifecycleOwner) {
            it?.getContentIfNotHandled()?.let { message ->
                binding.root.snackbar(message)
            }
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
            findListener: suspend (name: String) -> Flow<Result<RemoteCity?>?>,
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

                if(s?.isBlank() == true) {
                    apply("Enter the city name", Color.BLACK)
                    return
                }

                job = lifecycleScope.launch {
                    findListener.invoke(s?.toString()?.trim() ?: "").collect {
                        if(it == null) return@collect
                        when(it.status) {
                            Result.Status.SUCCESS -> {
                                remoteCity = it.data!!
                                apply("Found: ${remoteCity?.name}", Color.BLACK)
                            }

                            Result.Status.ERROR ->
                                apply(it.message ?: "Something went wrong", Color.RED)

                            Result.Status.LOADING ->
                                apply("Searching...", Color.BLACK)
                        }
                    }
                }
            }

            private fun apply(message: String, color: Int) {
                dialogBinding.txtName.apply {
                    text = message
                    setTextColor(color)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        AlertDialog.Builder(context)
                .setPositiveButton("Add") { _, _->
                    if(remoteCity == null) {
                        viewModel.showError("No city found!")
                    } else {
                        saveListener.invoke(remoteCity!!)
                    }
                }
                .setNegativeButton(android.R.string.cancel, null)
                .setView(dialogBinding.root)
                .create()
                .show()
    }

    @SuppressLint("MissingPermission")
    private fun saveLastLocationAsCity() {
        val snackbarPermissionListener = SnackbarOnDeniedPermissionListener.Builder
                .with(view, "Location permission is needed for detecting your city location")
                .withOpenSettingsButton("Settings")
                .build()

        val permissionListener = object : PermissionListener {
            override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                fusedLocationClient.lastLocation
                        .addOnSuccessListener { location: Location? ->
                            if(location == null) {
                                viewModel.showError("No location has been registered")
                            } else {
                                viewModel.insertCity(location.latitude, location.longitude)
                            }
                        }.addOnFailureListener {
                            viewModel.showError("Could not get location due to ${it.message}")
                        }
            }
            override fun onPermissionDenied(p0: PermissionDeniedResponse?) {}
            override fun onPermissionRationaleShouldBeShown(request: PermissionRequest?, token: PermissionToken?) {
                token?.continuePermissionRequest()
            }
        }

        val compositePermissionListener = CompositePermissionListener(
                snackbarPermissionListener,
                permissionListener
        )

        Dexter.withContext(context)
                .withPermission(locationPermission)
                .withListener(compositePermissionListener)
                .check()
    }

}
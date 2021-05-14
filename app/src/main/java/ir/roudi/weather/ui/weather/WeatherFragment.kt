package ir.roudi.weather.ui.weather

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import ir.roudi.weather.data.Repository
import ir.roudi.weather.data.local.db.AppDatabase
import ir.roudi.weather.data.local.pref.SharedPrefHelper
import ir.roudi.weather.data.remote.RetrofitHelper
import ir.roudi.weather.databinding.DialogWeatherDetailsBinding
import ir.roudi.weather.databinding.FragmentWeatherBinding

class WeatherFragment : Fragment() {

    private lateinit var binding : FragmentWeatherBinding

    private val viewModel by viewModels<WeatherViewModel> {
        val db = AppDatabase.getInstance(requireContext())
        val pref = SharedPrefHelper(requireContext())
        val repository = Repository(db.cityDao, db.weatherDao, RetrofitHelper.service, pref)
        WeatherViewModel.Factory(repository)
    }

    private val moreDetailsDialog : AlertDialog by lazy {
        val dialogBinding = DialogWeatherDetailsBinding.inflate(layoutInflater, binding.root as ViewGroup, false)
        dialogBinding.lifecycleOwner = viewLifecycleOwner
        dialogBinding.weather = viewModel.uiWeather

        AlertDialog.Builder(context)
                .setView(dialogBinding.root)
                .setTitle("More Details")
                .setPositiveButton(android.R.string.ok, null)
                .create()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setupBinding(inflater, container)

        viewModel.actionShowMoreDetailsDialog.observe(viewLifecycleOwner) { shouldShow ->
            if(shouldShow) {
                moreDetailsDialog.show()
                viewModel.showingMoreDetailsDialogCompleted()
            }
        }

        return binding.root
    }

    private fun setupBinding(inflater: LayoutInflater, container: ViewGroup?) {
        binding = FragmentWeatherBinding.inflate(inflater, container, false)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
    }

}
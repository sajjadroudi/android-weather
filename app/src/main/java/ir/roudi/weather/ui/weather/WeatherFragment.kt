package ir.roudi.weather.ui.weather

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import ir.roudi.weather.databinding.DialogWeatherDetailsBinding
import ir.roudi.weather.databinding.FragmentWeatherBinding

@AndroidEntryPoint
class WeatherFragment : Fragment() {

    private lateinit var binding : FragmentWeatherBinding

    private val viewModel : WeatherViewModel by viewModels()

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

        viewModel.actionShowMoreDetailsDialog.observe(viewLifecycleOwner) {
            it?.getContentIfNotHandled()?.let { shouldShow ->
                if(shouldShow) moreDetailsDialog.show()
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
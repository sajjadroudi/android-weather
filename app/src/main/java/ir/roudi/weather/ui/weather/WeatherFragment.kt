package ir.roudi.weather.ui.weather

import android.graphics.drawable.Drawable
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
import ir.roudi.weather.databinding.FragmentWeatherBinding
import java.io.IOException

class WeatherFragment : Fragment() {

    private val viewModel by viewModels<WeatherViewModel> {
        val db = AppDatabase.getInstance(requireContext())
        val repository = Repository(db.cityDao, db.weatherDao, RetrofitHelper.service, SharedPrefHelper(requireContext()))
        WeatherViewModel.Factory(repository)
    }

    private lateinit var binding : FragmentWeatherBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setupBinding(inflater, container)

        viewModel.weather.observe(viewLifecycleOwner) {
            it ?: return@observe
            try {
                val fileName = "img_${it.iconId}.png"
                val inputStream = requireContext().assets.open(fileName)
                val drawable = Drawable.createFromStream(inputStream, null)
                binding.imgWeather.setImageDrawable(drawable)
            } catch (ex: IOException) {
                ex.printStackTrace()
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
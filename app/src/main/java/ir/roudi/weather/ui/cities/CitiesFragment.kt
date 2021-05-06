package ir.roudi.weather.ui.cities

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import ir.roudi.weather.data.Repository
import ir.roudi.weather.data.local.AppDatabase
import ir.roudi.weather.data.local.entity.City
import ir.roudi.weather.data.remote.RetrofitHelper
import ir.roudi.weather.databinding.FragmentCitiesBinding

class CitiesFragment : Fragment() {

    private val viewModel : CitiesViewModel by lazy {
        val database = AppDatabase.getInstance(requireContext())
        val repository = Repository(database.cityDao, database.weatherDao, RetrofitHelper.service)
        ViewModelProvider(this, CitiesViewModel.Factory(repository))
            .get(CitiesViewModel::class.java)
    }

    private val adapter : CitiesAdapter by lazy {
        CitiesAdapter(object : CitiesAdapter.AdapterCallback {
            override fun onClick(city: City) {
                // TODO
                Toast.makeText(context, "${city.name} clicked!", Toast.LENGTH_SHORT).show()
            }

            override fun onDelete(city: City) {
                viewModel.deleteCity(city)
            }
        })
    }

    private lateinit var binding : FragmentCitiesBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCitiesBinding.inflate(inflater, container, false)

        binding.recyclerView.adapter = adapter
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

}
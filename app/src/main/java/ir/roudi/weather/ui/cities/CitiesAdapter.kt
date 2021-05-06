package ir.roudi.weather.ui.cities

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ir.roudi.weather.data.local.entity.City
import ir.roudi.weather.databinding.AdapterCityBinding

class CitiesAdapter(
    private val callback: AdapterCallback
) : ListAdapter<City, CitiesAdapter.CityViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityViewHolder {
        return CityViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: CityViewHolder, position: Int) {
        val cityItem = getItem(position)
        holder.bind(cityItem, 0, callback)
    }

    class CityViewHolder private constructor(
        private val binding: AdapterCityBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(city: City, selectedCityId: Int, callback: AdapterCallback) {
            binding.city = city
            binding.selectedCityId = selectedCityId
            binding.callback = callback
        }

        companion object {
            fun from(parent: ViewGroup): CityViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val binding = AdapterCityBinding.inflate(inflater, parent, false)
                return CityViewHolder(binding)
            }
        }
    }

    // TODO: check true implementation
    class DiffCallback : DiffUtil.ItemCallback<City>() {
        override fun areItemsTheSame(oldItem: City, newItem: City): Boolean {
            return oldItem.cityId == newItem.cityId
        }

        override fun areContentsTheSame(oldItem: City, newItem: City): Boolean {
            return oldItem == newItem
        }
    }

    interface AdapterCallback {
        fun onClick(city: City)
        fun onDelete(city: City)
    }
}
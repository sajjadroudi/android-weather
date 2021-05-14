package ir.roudi.weather.ui.cities

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ir.roudi.weather.data.local.db.entity.City
import ir.roudi.weather.databinding.AdapterCityBinding

class CitiesAdapter(
    private val callback: ItemCallback
) : ListAdapter<City, CitiesAdapter.CityViewHolder>(DiffCallback()) {

    var selectedCityId : Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityViewHolder {
        return CityViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: CityViewHolder, position: Int) {
        val cityItem = getItem(position)
        holder.bind(cityItem, selectedCityId, callback)
    }

    fun notifySelectedCityChanged(oldCityId: Int, newCityId: Int) {
        var index = currentList.indexOfFirst { it.cityId == oldCityId }
        if(index >= 0) notifyItemChanged(index)

        index = currentList.indexOfFirst { it.cityId == newCityId }
        if(index >= 0) notifyItemChanged(index)
    }

    class CityViewHolder private constructor(
        private val binding: AdapterCityBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(city: City, selectedCityId: Int, callback: ItemCallback) {
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

    class DiffCallback : DiffUtil.ItemCallback<City>() {
        override fun areItemsTheSame(oldItem: City, newItem: City): Boolean {
            return oldItem.cityId == newItem.cityId
        }

        override fun areContentsTheSame(oldItem: City, newItem: City): Boolean {
            return newItem.name == oldItem.name
        }
    }

    interface ItemCallback {
        fun onClick(city: City)
        fun onDelete(city: City)
        fun onEdit(city: City)
    }
}
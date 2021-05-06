package ir.roudi.weather.utils

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import ir.roudi.weather.data.local.db.entity.City
import ir.roudi.weather.ui.cities.CitiesAdapter

@BindingAdapter("cityList")
fun RecyclerView.setCityList(cities: List<City>?) {
    (adapter as CitiesAdapter).submitList(cities)
}
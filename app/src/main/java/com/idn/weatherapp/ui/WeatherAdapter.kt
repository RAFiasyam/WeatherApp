package com.idn.weatherapp.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.idn.weatherapp.BuildConfig
import com.idn.weatherapp.R
import com.idn.weatherapp.data.response.ListItem
import com.idn.weatherapp.databinding.RowItemWeatherBinding
import com.idn.weatherapp.utils.HelperFunctions.formatDegree
import com.idn.weatherapp.utils.iconSizeWeather2x
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class WeatherAdapter: RecyclerView.Adapter<WeatherAdapter.MyViewHolder>() {
    val listForecast = ArrayList<ListItem>()
    class MyViewHolder(val binding: RowItemWeatherBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)= MyViewHolder(
        RowItemWeatherBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val data = listForecast[position]
        holder.binding.apply {
            val maxTemp = "Max: " + formatDegree(data.main?.tempMax)
            val minTemp = "Min: " + formatDegree(data.main?.tempMin)
            tvMaxDegree.text = maxTemp
            tvMinDegree.text = minTemp

            val urlIconWeather =
                BuildConfig.ICON_BASE_URL + data.weather?.get(0)?.icon + iconSizeWeather2x
            Glide.with(imgItemWeather.context).load(urlIconWeather)
                .placeholder(R.drawable.ic_broken_image)
                .error(R.drawable.ic_broken_image)
                .into(imgItemWeather)

            val date = data.dtTxt?.take(10)
            val time = data.dtTxt?.takeLast(8)
            val dateArray = date?.split("-")?.toTypedArray()
            val timeArray = time?.split(":")?.toTypedArray()

            Log.i("WeatherAdapter", "date: $date  ----- time: $time")

            val calendar = Calendar.getInstance()
            calendar.set(Calendar.YEAR, Integer.parseInt(dateArray?.get(0) as String))
            calendar.set(Calendar.MONTH, Integer.parseInt(dateArray[1]) - 1)
            calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateArray[2]))
            calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeArray?.get(0) as String))
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)

            val dateResultFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
                .format(calendar.time).toString()
            val timeResultFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                .format(calendar.time).toString()

            tvItemDate.text = dateResultFormat
            tvItemTime.text = timeResultFormat
        }
    }

    override fun getItemCount() = listForecast.size
    fun setData(data: List<ListItem>?) {
        if (data == null) return
        listForecast.clear()
        listForecast.addAll(data)
    }
}
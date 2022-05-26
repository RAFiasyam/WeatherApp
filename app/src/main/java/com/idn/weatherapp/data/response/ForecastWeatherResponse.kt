package com.idn.weatherapp.data.response

import com.google.gson.annotations.SerializedName

data class ForecastWeatherResponse(

	@field:SerializedName("list")
	val list: List<ListItem>? = null
)

data class ListItem(


	@field:SerializedName("dt_txt")
	val dtTxt: String? = null,

	@field:SerializedName("weather")
	val weather: List<WeatherItem?>? = null,

	@field:SerializedName("main")
	val main: Main? = null,

)

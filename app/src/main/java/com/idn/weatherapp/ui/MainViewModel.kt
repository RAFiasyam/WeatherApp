package com.idn.weatherapp.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.idn.weatherapp.data.network.ApiConfig
import com.idn.weatherapp.data.response.ForecastWeatherResponse
import com.idn.weatherapp.data.response.WeatherResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainViewModel : ViewModel() {

    private val weatherByCity = MutableLiveData<WeatherResponse>()
    private val forecastByCity = MutableLiveData<ForecastWeatherResponse>()

    private val weatherByCurrentLocation = MutableLiveData<WeatherResponse>()
    private val forecastByCurrentLocation = MutableLiveData<ForecastWeatherResponse>()


    fun weatherByCity(city: String) {
        ApiConfig.getApiService().weatherByCity(city)
            .enqueue(object : Callback<WeatherResponse> {
                override fun onResponse(
                    call: Call<WeatherResponse>,
                    response: Response<WeatherResponse>
                ) {
                    if (response.isSuccessful) {
                        weatherByCity.postValue(response.body())
                    }
                }

                override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                    Log.d("Failure", t.message.toString())
                }
            }
            )
    }

    fun getWeatherByCity(): LiveData<WeatherResponse> = weatherByCity

    fun forecastByCity(city: String) {
        ApiConfig.getApiService().forecastByCity(city).enqueue(object :
            Callback<ForecastWeatherResponse> {
            override fun onResponse(
                call: Call<ForecastWeatherResponse>,
                response: Response<ForecastWeatherResponse>
            ) {
                if (response.isSuccessful) forecastByCity.postValue(response.body())
            }

            override fun onFailure(call: Call<ForecastWeatherResponse>, t: Throwable) {
                TODO("Not yet implemented")
            }
        })
    }

    fun getForecastWeatherByCity(): LiveData<ForecastWeatherResponse> = forecastByCity

    fun weatherByCurrentLocation(lat: Double, lon: Double) {
        ApiConfig.getApiService().weatherByCurrentLocation(lat, lon)
            .enqueue(object : Callback<WeatherResponse>{
                override fun onResponse(
                    call: Call<WeatherResponse>,
                    response: Response<WeatherResponse>
                ) {
                    if (response.isSuccessful) weatherByCurrentLocation.postValue(response.body())
                }

                override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                    TODO("Not yet implemented")
                }
            })
    }

    fun getWeatherByCurrentLocation(): LiveData<WeatherResponse> = weatherByCurrentLocation

    fun forecastByCurrentLocation(lat: Double, lon: Double) {
        ApiConfig.getApiService().forecastByCurrentLocation(lat, lon)
            .enqueue(object : Callback<ForecastWeatherResponse> {
                override fun onResponse(
                    call: Call<ForecastWeatherResponse>,
                    response: Response<ForecastWeatherResponse>
                ) {
                    if (response.isSuccessful) forecastByCurrentLocation.postValue(response.body())
                }

                override fun onFailure(call: Call<ForecastWeatherResponse>, t: Throwable) {
                    TODO("Not yet implemented")
                }

            })
    }

    fun getForecastWeatherByLocation(): LiveData<ForecastWeatherResponse> = forecastByCurrentLocation
}
package com.idn.weatherapp.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.idn.weatherapp.BuildConfig
import com.idn.weatherapp.R
import com.idn.weatherapp.data.response.ForecastWeatherResponse
import com.idn.weatherapp.data.response.WeatherResponse
import com.idn.weatherapp.databinding.ActivityMainBinding
import com.idn.weatherapp.utils.HelperFunctions.formatDegree
import com.idn.weatherapp.utils.LOCATION_PERMISSION_REQ_CODE
import com.idn.weatherapp.utils.iconSizeWeather4x
import java.util.*

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding as ActivityMainBinding

    private var _viewModel: MainViewModel? = null
    private val viewModel get() = _viewModel as MainViewModel

    private val weatherAdapter by lazy { WeatherAdapter() }

    private var _fusedLocation: FusedLocationProviderClient? = null
    private val fusedLocation get() = _fusedLocation as FusedLocationProviderClient

    private var _lat: Double? = null
    private val lat get() = _lat as Double

    private var _lon: Double? = null
    private val lon get() = _lon as Double

    private var isResultFromSearch: Boolean = false
    private var isLoading: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setAppAsFullscreen()

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        _viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        searchCity()
        getWeatherByCity()
        getWeatherByCurrentLocation()
    }

    private fun getWeatherByCurrentLocation() {
        isLoading = true
        loadingStateView()
        _fusedLocation = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                LOCATION_PERMISSION_REQ_CODE
            )
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }

        fusedLocation.lastLocation
            .addOnSuccessListener {
                _lat = it.latitude
                Log.i("MainActivity", "currentLatitude: $_lat")
                _lon = it.longitude
                Log.i("MainActivity", "currentLongitude: $_lon")
                viewModel.weatherByCurrentLocation(lat, lon)
                viewModel.forecastByCurrentLocation(lat, lon)
            }

            .addOnFailureListener {
                Toast.makeText(this, "Failed on getting current location.", Toast.LENGTH_SHORT)
                    .show()
            }

        viewModel.getWeatherByCurrentLocation().observe(this) {
            setupView(it, null)
        }
        viewModel.getForecastWeatherByLocation().observe(this@MainActivity) {
            setupView(null, it)
            isLoading = false
            loadingStateView()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQ_CODE -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    // permission granted
                } else {
                    // permission denied
                    Toast.makeText(
                        this, "You need to grant permission to access location",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun getWeatherByCity() {
        viewModel.getWeatherByCity().observe(this) {
            Log.i("MainActivity", "WeatherByCity: ${it.name}")
            setupView(it, null)
        }
        viewModel.getForecastWeatherByCity().observe(this) {
            setupView(null, it)
        }
    }

    private fun setupView(weather: WeatherResponse?, forecast: ForecastWeatherResponse?) {
        weather?.let {
            binding.apply {
                when (isResultFromSearch) {
                    true -> {
                        tvCity.text = weather.name
                    }
                    else -> {
                        val geoCoder = Geocoder(applicationContext, Locale.getDefault())
                        val address = geoCoder.getFromLocation(lat, lon, 1)
                        val fullAddress = address[0].getAddressLine(0)
                        Log.i("MainActivity", "fullAddressFromGeocoder: $fullAddress")
                        val city = address[0].locality
                        tvCity.text = city
                    }
                }
                Log.i("MainActivity", "degree: ${it.main?.temp}")
                tvDegree.text = formatDegree(it.main?.temp)

                val icon = it.weather?.get(0)?.icon
                val urlIconWeather = BuildConfig.ICON_BASE_URL + icon + iconSizeWeather4x
                Glide.with(applicationContext).load(urlIconWeather)
                    .placeholder(R.drawable.ic_broken_image)
                    .error(R.drawable.ic_broken_image)
                    .into(imgIcWeather)

                setupBackgroundPage(it.weather?.get(0)?.id, icon)
            }
        }
        forecast?.list.let {
            binding.rvWeather.apply {
                weatherAdapter.setData(it)
                adapter = weatherAdapter
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            }
        }
    }

    private fun setupBackgroundPage(id: Int?, icon: String?) {
        id?.let {
            when (id) {
                in resources.getIntArray(R.array.thunderstorm_id_list) -> setBackgroundImage(R.drawable.thunderstorm)
                in resources.getIntArray(R.array.drizzle_id_list) -> setBackgroundImage(R.drawable.drizzle)
                in resources.getIntArray(R.array.rain_id_list) -> setBackgroundImage(R.drawable.rain)
                in resources.getIntArray(R.array.freezing_rain_id_list) -> setBackgroundImage(R.drawable.freezing_rain)
                in resources.getIntArray(R.array.snow_id_list) -> setBackgroundImage(R.drawable.snow)
                in resources.getIntArray(R.array.sleet_id_list) -> setBackgroundImage(R.drawable.sleet)
                in resources.getIntArray(R.array.clear_id_list) -> {
                    when (icon) {
                        "01d" -> setBackgroundImage(R.drawable.clear)
                        "01n" -> setBackgroundImage(R.drawable.clear_night)
                    }
                }
                in resources.getIntArray(R.array.clouds_id_list) -> setBackgroundImage(R.drawable.lightcloud)
                in resources.getIntArray(R.array.heavy_clouds_id_list) -> setBackgroundImage(R.drawable.heavycloud)
                in resources.getIntArray(R.array.fog_id_list) -> setBackgroundImage(R.drawable.fog)
                in resources.getIntArray(R.array.sand_id_list) -> setBackgroundImage(R.drawable.sand)
                in resources.getIntArray(R.array.dust_id_list) -> setBackgroundImage(R.drawable.dust)
                in resources.getIntArray(R.array.volcanic_ash_id_list) -> setBackgroundImage(R.drawable.volcanic)
                in resources.getIntArray(R.array.squalls_id_list) -> setBackgroundImage(R.drawable.squalls)
                in resources.getIntArray(R.array.tornado_id_list) -> setBackgroundImage(R.drawable.tornado)
            }
        }
    }

    private fun setBackgroundImage(image: Int) {
        Glide.with(applicationContext).load(image).into(binding.imgBgWeather)
    }

    private fun searchCity() {
        binding.edtSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    isLoading = true
                    loadingStateView()
                    try {
                        val inputMethodManager =
                            getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                        inputMethodManager.hideSoftInputFromWindow(binding.root.windowToken, 0)
                    } catch (e: Throwable) {
                        Log.e("MainActivity", e.toString())
                    }
                    viewModel.weatherByCity(it)
                    viewModel.forecastByCity(it)
                }
                isResultFromSearch = true
                isLoading = false
                loadingStateView()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    viewModel.weatherByCity(it)
                    viewModel.forecastByCity(it)
                }
                isResultFromSearch = true
                return true
            }
        })
    }

    private fun loadingStateView() {
        binding.apply {
            when (isLoading) {
                true -> {
                    layoutWeather.visibility = View.INVISIBLE
                    progressBar.visibility = View.VISIBLE
                }
                false -> {
                    layoutWeather.visibility = View.VISIBLE
                    progressBar.visibility = View.INVISIBLE
                }
                else -> {
                    layoutWeather.visibility = View.INVISIBLE
                    progressBar.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setAppAsFullscreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val windowInsetsController = ViewCompat.getWindowInsetsController(window.decorView)
        windowInsetsController?.isAppearanceLightNavigationBars = true
    }
}
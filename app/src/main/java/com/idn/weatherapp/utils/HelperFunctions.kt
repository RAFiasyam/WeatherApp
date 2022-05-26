package com.idn.weatherapp.utils

import java.math.RoundingMode

object HelperFunctions {

    fun formatDegree(temperature: Double?): String {
        val maxTemp = temperature as Double
        val tempToCelsius = maxTemp - 273.0
        val formatDegree = tempToCelsius.toBigDecimal().setScale(2,RoundingMode.CEILING).toDouble()
        return  "$formatDegree℃"
    }
}
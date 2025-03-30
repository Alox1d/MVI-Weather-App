package com.sumin.weatherapp.data.local.mapper

import android.icu.util.Calendar
import com.sumin.weatherapp.data.network.dto.WeatherCurrentDto
import com.sumin.weatherapp.data.network.dto.WeatherDto
import com.sumin.weatherapp.data.network.dto.WeatherForecastDto
import com.sumin.weatherapp.domain.entity.Forecast
import com.sumin.weatherapp.domain.entity.Weather
import java.util.Date

fun WeatherCurrentDto.toEntity() = current.toEntity()

fun WeatherDto.toEntity() = Weather(
    tempC = tempC,
    conditionText = condition.text,
    conditionUrl = condition.iconUrl.correctImageUrl(),
    date = date.toCalender(),
)

fun WeatherForecastDto.toEntity() = Forecast(
    currentWeather = current.toEntity(),
    upcoming = forecastDto.forecastDay
        .drop(1)
        .map { dayDto ->
        val dayWeatherDto = dayDto.dayWeatherDto
        Weather(
            tempC = dayWeatherDto.tempC,
            conditionText = dayWeatherDto.conditionDto.text,
            conditionUrl = dayWeatherDto.conditionDto.iconUrl.correctImageUrl(),
            date = dayDto.date.toCalender(),
        )
    },
)

private fun Long.toCalender() = Calendar.getInstance().apply {
    time = Date(this@toCalender * 1000)
}

private fun String.correctImageUrl() = "https:$this".replace(
    oldValue = "64x64",
    newValue = "128x128",
)
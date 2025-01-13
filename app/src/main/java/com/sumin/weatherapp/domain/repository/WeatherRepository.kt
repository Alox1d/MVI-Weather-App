package com.sumin.weatherapp.domain.repository

import com.sumin.weatherapp.domain.entity.City
import com.sumin.weatherapp.domain.entity.Weather
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {

    suspend fun getWeather(cityId: Int): Weather

    suspend fun getForecast(cityId: Int): Weather
}
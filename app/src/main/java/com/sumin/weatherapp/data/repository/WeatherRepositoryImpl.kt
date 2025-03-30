package com.sumin.weatherapp.data.repository

import com.sumin.weatherapp.data.local.mapper.toEntity
import com.sumin.weatherapp.data.network.api.ApiService
import com.sumin.weatherapp.domain.entity.Forecast
import com.sumin.weatherapp.domain.entity.Weather
import com.sumin.weatherapp.domain.repository.WeatherRepository
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
) : WeatherRepository {
    override suspend fun getWeather(cityId: Int): Weather {
        return apiService.loadCurrentWeather(query = PREFIX_CITY_ID + cityId).toEntity()
    }

    override suspend fun getForecast(cityId: Int): Forecast {
        return apiService.loadForecast(query = PREFIX_CITY_ID + cityId).toEntity()
    }

    private companion object {
        const val PREFIX_CITY_ID = "id:"
    }
}
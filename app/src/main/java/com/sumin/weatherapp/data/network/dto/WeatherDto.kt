package com.sumin.weatherapp.data.network.dto

import com.google.gson.annotations.SerializedName

data class WeatherDto(
    @SerializedName("last_updated_epoch") val data: Long,
    @SerializedName("temp_c") val tempC: Long,
    @SerializedName("condition") val condition: ConditionDto,
)

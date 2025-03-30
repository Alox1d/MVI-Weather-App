package com.sumin.weatherapp.data.local.mapper

import com.sumin.weatherapp.data.network.dto.CityDto
import com.sumin.weatherapp.domain.entity.City

fun CityDto.toEntity() = City(
    id = id,
    name = name,
    country = country,
)

fun List<CityDto>.toEntities() = map { it.toEntity() }
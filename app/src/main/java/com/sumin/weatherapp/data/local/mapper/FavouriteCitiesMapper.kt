package com.sumin.weatherapp.data.local.mapper

import com.sumin.weatherapp.data.local.model.CityDbModel
import com.sumin.weatherapp.domain.entity.City

fun City.toDbModel(): CityDbModel = CityDbModel(
    id = id,
    name = name,
    country = country,
)

fun CityDbModel.toEntity() : City = City(
    id = id,
    name = name,
    country = country
)

fun List<CityDbModel>.toEntities(): List<City> = map { it.toEntity() }
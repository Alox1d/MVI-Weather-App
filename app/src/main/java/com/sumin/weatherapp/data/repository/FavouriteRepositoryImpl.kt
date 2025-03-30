package com.sumin.weatherapp.data.repository

import com.sumin.weatherapp.data.local.db.FavouriteCitiesDao
import com.sumin.weatherapp.data.local.mapper.toDbModel
import com.sumin.weatherapp.data.local.mapper.toEntities
import com.sumin.weatherapp.data.local.model.CityDbModel
import com.sumin.weatherapp.domain.entity.City
import com.sumin.weatherapp.domain.repository.FavouriteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FavouriteRepositoryImpl @Inject constructor(
    private val favouriteCitiesDao: FavouriteCitiesDao,
) : FavouriteRepository {
    override val favouriteCities: Flow<List<City>> =
        favouriteCitiesDao
            .getFavouriteCities()
            .map { it.toEntities() }

    override fun observeIsFavourite(cityId: Int): Flow<Boolean> =
        favouriteCitiesDao.observeIsFavourite(cityId)

    override suspend fun addToFavourite(city: City) =
        favouriteCitiesDao.addToFavourites(city.toDbModel())

    override suspend fun removeFromFavourite(cityId: Int) =
        favouriteCitiesDao.removeFromFavourite(cityId)
}
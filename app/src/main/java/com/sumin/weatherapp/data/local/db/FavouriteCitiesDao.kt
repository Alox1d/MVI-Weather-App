package com.sumin.weatherapp.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sumin.weatherapp.data.local.model.CityDbModel
import com.sumin.weatherapp.data.network.dto.CityDto
import kotlinx.coroutines.flow.Flow

@Dao
interface FavouriteCitiesDao {

    @Query("SELECT * FROM favourite_cities")
    fun getFavouriteCities(): Flow<List<CityDto>>

    @Query("SELECT EXISTS(SELECT 1 FROM favourite_cities WHERE id=:cityId)")
    fun observeIsFavourite(cityId: Int): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToFavourites(cityDbModel: CityDbModel)

    @Query("DELETE FROM favourite_cities WHERE id=:cityId")
    suspend fun removeFromFavourite(cityId: Int)
}
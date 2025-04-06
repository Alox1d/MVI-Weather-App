package com.sumin.weatherapp.presentation.favourite

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.bumptech.glide.Glide.init
import com.sumin.weatherapp.domain.entity.City
import com.sumin.weatherapp.presentation.extensions.componentScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class DefaultFavouriteComponent @Inject constructor(
    private val favouriteStoreFactory: FavouriteStoreFactory,
    private val onItemCityClick: (City) -> Unit, // заинжектить коллебки - отдельная тема
    private val onAddFavouriteClick: () -> Unit,
    private val onSearchClick: () -> Unit,
    componentContext: ComponentContext,
) : FavouriteComponent, ComponentContext by componentContext {

    private val store = instanceKeeper.getStore { favouriteStoreFactory.create() }
    private val scope = componentScope()

    init {
        scope.launch {
            store.labels.collect {
                when(it){
                    is FavouriteStore.Label.CityItemClick -> {
                        onItemCityClick(it.city)
                    }
                    FavouriteStore.Label.ClickSearch -> {
                        onSearchClick()
                    }
                    FavouriteStore.Label.ClickToFavourite -> {
                        onAddFavouriteClick()
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val model: StateFlow<FavouriteStore.State>
        get() = store.stateFlow

    override fun onClickSearch() {
        store.accept(FavouriteStore.Intent.ClickSearch)
    }

    override fun onClickAddFavourite() {
        store.accept(FavouriteStore.Intent.ClickAddFavourite)
    }

    override fun onCityItemClick(city: City) {
        store.accept(FavouriteStore.Intent.CityItemClick(city))
    }
}
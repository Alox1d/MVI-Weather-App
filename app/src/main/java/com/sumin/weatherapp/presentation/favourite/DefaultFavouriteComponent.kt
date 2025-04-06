package com.sumin.weatherapp.presentation.favourite

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.sumin.weatherapp.domain.entity.City
import com.sumin.weatherapp.presentation.extensions.componentScope
import com.sumin.weatherapp.presentation.search.DefaultSearchComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class DefaultFavouriteComponent @AssistedInject constructor(
    private val storeFactory: FavouriteStoreFactory,
    @Assisted("onItemCityClick") onItemCityClick: (City) -> Unit, // заинжектить коллебки - отдельная тема
    @Assisted("onAddFavouriteClick") onAddFavouriteClick: () -> Unit,
    @Assisted("onSearchClick") onSearchClick: () -> Unit,
    @Assisted("componentContext") componentContext: ComponentContext,
) : FavouriteComponent, ComponentContext by componentContext {

    private val store = instanceKeeper.getStore { storeFactory.create() }
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

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("onItemCityClick") onItemCityClick: (City) -> Unit, // заинжектить коллебки - отдельная тема
            @Assisted("onAddFavouriteClick") onAddFavouriteClick: () -> Unit,
            @Assisted("onSearchClick") onSearchClick: () -> Unit,
            @Assisted("componentContext") componentContext: ComponentContext,
        ): DefaultFavouriteComponent
    }
}
package com.sumin.weatherapp.presentation.search

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.sumin.weatherapp.domain.entity.City
import com.sumin.weatherapp.presentation.extensions.componentScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class DefaultSearchComponent @AssistedInject constructor(
    private val storeFactory: SearchStoreFactory,
    @Assisted("openReason") openReason: OpenReason,
    @Assisted("onBackClick") onBackClick: () -> Unit, // т.к. совпадают возвращаемые типы
    @Assisted("onCitySavedToFavourite") onCitySavedToFavourite: () -> Unit,
    @Assisted("onForecastRequested") onForecastRequested: (City) -> Unit,
    @Assisted("componentContext") componentContext: ComponentContext,
) : SearchComponent, ComponentContext by componentContext {

    private val store = instanceKeeper.getStore { storeFactory.create(openReason) }
    private val scope = componentScope()

    init {
        scope.launch {
            store.labels.collect {
                when(it){
                    SearchStore.Label.ClickBack -> {
                        onBackClick()
                    }
                    is SearchStore.Label.OpenForecast -> {
                        onForecastRequested(it.city)
                    }
                    SearchStore.Label.SavedToFavourite -> {
                        onCitySavedToFavourite()
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val model: StateFlow<SearchStore.State>
        get() = store.stateFlow

    override fun changeSearchQuery(query: String) {
        store.accept(SearchStore.Intent.ChangeSearchQuery(query))
    }

    override fun onClickBack() {
        store.accept(SearchStore.Intent.CLickBack)
    }

    override fun onClickSearch() {
        store.accept(SearchStore.Intent.CLickSearch)
    }

    override fun onClickCity(city: City) {
        store.accept(SearchStore.Intent.CLickCity(city))
    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("openReason") openReason: OpenReason,
            @Assisted("onBackClick") onBackClick: () -> Unit, // т.к. совпадают возвращаемые типы
            @Assisted("onCitySavedToFavourite") onCitySavedToFavourite: () -> Unit,
            @Assisted("onForecastRequested") onForecastRequested: (City) -> Unit,
            @Assisted("componentContext") componentContext: ComponentContext,
        ): DefaultSearchComponent
    }
}
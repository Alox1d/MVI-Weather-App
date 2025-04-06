package com.sumin.weatherapp.presentation.search

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.sumin.weatherapp.domain.entity.City
import com.sumin.weatherapp.presentation.extensions.componentScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class DefaultSearchComponent @Inject constructor(
    private val openReason: OpenReason,
    private val storeFactory: SearchStoreFactory,
    private val onBackClick: () -> Unit,
    private val onCitySavedToFavourite: () -> Unit,
    private val onForecastRequested: (City) -> Unit,
    componentContext: ComponentContext,
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
}
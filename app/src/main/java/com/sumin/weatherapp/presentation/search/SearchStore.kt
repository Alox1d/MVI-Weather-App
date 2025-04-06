package com.sumin.weatherapp.presentation.search

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.sumin.weatherapp.domain.entity.City
import com.sumin.weatherapp.domain.usecase.ChangeFavouriteStateUseCase
import com.sumin.weatherapp.domain.usecase.SearchCityUseCase
import com.sumin.weatherapp.presentation.favourite.FavouriteStore
import com.sumin.weatherapp.presentation.search.SearchStore.Intent
import com.sumin.weatherapp.presentation.search.SearchStore.Label
import com.sumin.weatherapp.presentation.search.SearchStore.State
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

interface SearchStore : Store<Intent, State, Label> {

    sealed interface Intent {

        data class ChangeSearchQuery(val query: String) : Intent

        data object CLickBack : Intent
        data object CLickSearch : Intent
        data class CLickCity(val city: City) : Intent
    }

    data class State(
        val searchQuery: String,
        val searchState: SearchState,
    ) {

        sealed interface SearchState {

            data object Initial : SearchState
            data object Loading : SearchState
            data object Error : SearchState
            data object EmptyResult : SearchState
            data class SuccessLoaded(val cities: List<City>) : SearchState
        }
    }

    sealed interface Label {

        data object ClickBack : Label
        data object SavedToFavourite : Label
        data class OpenForecast(val city: City) : Label
    }
}

class SearchStoreFactory @Inject constructor(
    private val storeFactory: StoreFactory,
    private val searchCityUseCase: SearchCityUseCase,
    private val changeFavouriteStateUseCase: ChangeFavouriteStateUseCase,
) {

    fun create(openReason: OpenReason): SearchStore =
        object : SearchStore, Store<Intent, State, Label> by storeFactory.create(
            name = "SearchStore",
            initialState = State(
                searchQuery = "",
                searchState = State.SearchState.Initial,
            ),
            bootstrapper = BootstrapperImpl(),
            executorFactory = { ExecutorImpl(openReason) },
            reducer = ReducerImpl
        ) {}

    private sealed interface Action {
        // т.к. в бутстраппере ни на что не подписываемся,
        // Action'ов не будет
    }

    private sealed interface Msg {

        data class ChangeSearchQuery(val query: String) : Msg
        data object LoadingSearchResult : Msg
        data object ErrorSearchResult : Msg
        data class LoadedSearchResult(val cities: List<City>) : Msg
    }

    private class BootstrapperImpl : CoroutineBootstrapper<Action>() {
        override fun invoke() {
            // никак загрузок при создании Стора мы не выполняем
            // поэтому тут пусто :)
        }
    }

    private inner class ExecutorImpl(private val openReason: OpenReason) :
        CoroutineExecutor<Intent, Action, State, Msg, Label>() {

        private var searchJob: Job? = null

        override fun executeIntent(intent: Intent, getState: () -> State) {
            when (intent) {
                is Intent.ChangeSearchQuery -> {
                    dispatch(Msg.ChangeSearchQuery(intent.query))
                }

                Intent.CLickBack -> {
                    publish(Label.ClickBack)
                }

                is Intent.CLickCity -> {
                    when (openReason) {
                        OpenReason.AddToFavourite -> {
                            scope.launch {
                                changeFavouriteStateUseCase.addToFavourite(city = intent.city)
                                publish(Label.SavedToFavourite)
                            }
                        }

                        OpenReason.RegularSearch -> {
                            publish(Label.OpenForecast(intent.city))
                        }
                    }
                }

                Intent.CLickSearch -> {
                    searchJob?.cancel()
                    searchJob = scope.launch {
                        dispatch(Msg.LoadingSearchResult)
                        try {
                            val cities = searchCityUseCase(getState().searchQuery)
                            dispatch(Msg.LoadedSearchResult(cities))
                        } catch (e: Exception) {
                            dispatch(Msg.ErrorSearchResult)
                        }
                    }
                }
            }
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State = when (msg) {
            is Msg.ChangeSearchQuery -> {
                copy(searchQuery = msg.query)
            }

            Msg.ErrorSearchResult -> {
                copy(searchState = State.SearchState.Error)
            }

            is Msg.LoadingSearchResult -> {
                copy(searchState = State.SearchState.Loading)
            }

            is Msg.LoadedSearchResult -> {
                val searchState =
                    if (msg.cities.isEmpty()) {
                        State.SearchState.EmptyResult
                    } else State.SearchState.SuccessLoaded(
                        msg.cities
                    )
                copy(searchState = searchState)
            }
        }
    }
}

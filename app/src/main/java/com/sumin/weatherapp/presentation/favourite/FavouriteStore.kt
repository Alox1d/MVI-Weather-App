package com.sumin.weatherapp.presentation.favourite

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.sumin.weatherapp.domain.entity.City
import com.sumin.weatherapp.domain.usecase.GetCurrentWeatherUseCase
import com.sumin.weatherapp.domain.usecase.GetFavouriteCitiesUseCase
import com.sumin.weatherapp.presentation.favourite.FavouriteStore.Intent
import com.sumin.weatherapp.presentation.favourite.FavouriteStore.Label
import com.sumin.weatherapp.presentation.favourite.FavouriteStore.State
import kotlinx.coroutines.launch
import javax.inject.Inject

interface FavouriteStore : Store<Intent, State, Label> {

    sealed interface Intent {

        data object ClickSearch : Intent
        data object ClickAddFavourite : Intent
        data class CityItemClick(val city: City) : Intent
    }

    data class State(
        val cityItems: List<CityItem>,
    ) {

        data class CityItem(
            val city: City,
            val weatherState: WeatherState,
        )

        sealed interface WeatherState {

            data object Initial : WeatherState
            data object Loading : WeatherState
            data object Error : WeatherState

            data class Loaded(
                val tempC: Float,
                val iconUrl: String,
            ) : WeatherState
        }
    }

    sealed interface Label {

        data object ClickSearch : Label
        data object ClickToFavourite : Label
        data class CityItemClick(val city: City) : Label
    }
}

class FavouriteStoreFactory @Inject constructor(
    private val storeFactory: StoreFactory,
    private val getFavouriteCitiesUseCase: GetFavouriteCitiesUseCase,
    private val getCurrentWeatherUseCase: GetCurrentWeatherUseCase,
) {

    fun create(): FavouriteStore =
        object : FavouriteStore, Store<Intent, State, Label> by storeFactory.create(
            name = "FavouriteStore",
            initialState = State(
                cityItems = listOf(),
            ),
            bootstrapper = BootstrapperImpl(),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed interface Action {

        data class FavouriteCitiesLoaded(
            val cities: List<City>
        ) : Action
    }

    private sealed interface Msg {

        data class FavouriteCitiesLoaded(
            val cities: List<City>
        ) : Msg

        data class WeatherLoaded(
            val cityId: Int,
            val tempC: Float,
            val conditionIconUrl: String,
        ) : Msg

        data class WeatherLoadingError(
            val cityId: Int,
        ) : Msg

        data class WeatherLoading(
            val cityId: Int,
        ) : Msg
    }

    private inner class BootstrapperImpl : CoroutineBootstrapper<Action>() {
        override fun invoke() {
            scope.launch {
                getFavouriteCitiesUseCase().collect {
                    dispatch(Action.FavouriteCitiesLoaded(it))
                }
            }
        }
    }

    private inner class ExecutorImpl : CoroutineExecutor<Intent, Action, State, Msg, Label>() {
        override fun executeIntent(intent: Intent, getState: () -> State) {
            when (intent) {
                is Intent.CityItemClick -> {
                    publish(Label.CityItemClick(intent.city))
                }

                Intent.ClickSearch -> {
                    publish(Label.ClickSearch)
                }

                Intent.ClickAddFavourite -> {
                    publish(Label.ClickToFavourite)
                }
            }
        }

        override fun executeAction(action: Action, getState: () -> State) {
            when (action) {
                is Action.FavouriteCitiesLoaded -> {
                    val cities = action.cities
                    dispatch(Msg.FavouriteCitiesLoaded(cities))
                    cities.forEach {
                        scope.launch {
                            loadWeather(it)
                        }
                    }
                }
            }
        }

        private suspend fun loadWeather(city: City) {
            dispatch(Msg.WeatherLoading(city.id))

            try {
                val weather = getCurrentWeatherUseCase(city.id)
                dispatch(
                    Msg.WeatherLoaded(
                        cityId = city.id,
                        tempC = weather.tempC,
                        conditionIconUrl = weather.conditionUrl,
                    )
                )
            } catch (e: Exception) {
                dispatch(Msg.WeatherLoadingError(city.id))
            }
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State =
            when (msg) {
                is Msg.FavouriteCitiesLoaded -> {
                    copy(
                        cityItems = msg.cities.map {
                            State.CityItem(
                                city = it,
                                weatherState = State.WeatherState.Initial,
                            )
                        }
                    )
                }

                is Msg.WeatherLoaded -> {
                    copy(
                        cityItems = cityItems.map {
                            if (it.city.id == msg.cityId) {
                                it.copy(
                                    weatherState = State.WeatherState.Loaded(
                                        tempC = msg.tempC,
                                        iconUrl = msg.conditionIconUrl,
                                    )
                                )
                            } else {
                                it
                            }
                        }
                    )
                }

                is Msg.WeatherLoading -> {
                    copy(
                        cityItems = cityItems.map {
                            if (it.city.id == msg.cityId) {
                                it.copy(weatherState = State.WeatherState.Loading)
                            } else {
                                it
                            }
                        }
                    )
                }

                is Msg.WeatherLoadingError -> {
                    copy(
                        cityItems = cityItems.map {
                            if (it.city.id == msg.cityId) {
                                it.copy(weatherState = State.WeatherState.Error)
                            } else {
                                it
                            }
                        }
                    )
                }
            }
    }
}

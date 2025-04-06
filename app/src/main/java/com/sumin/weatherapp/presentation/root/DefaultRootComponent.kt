package com.sumin.weatherapp.presentation.root

import android.os.Parcelable
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.sumin.weatherapp.domain.entity.City
import com.sumin.weatherapp.presentation.details.DefaultDetailsComponent
import com.sumin.weatherapp.presentation.details.DefaultDetailsComponent_Factory
import com.sumin.weatherapp.presentation.details.DetailsStoreFactory
import com.sumin.weatherapp.presentation.favourite.DefaultFavouriteComponent
import com.sumin.weatherapp.presentation.search.DefaultSearchComponent
import com.sumin.weatherapp.presentation.search.OpenReason
import com.sumin.weatherapp.presentation.search.SearchComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

class DefaultRootComponent @AssistedInject constructor(
    private val detailsComponentFactory: DefaultDetailsComponent.Factory,
    private val favouriteComponentFactory: DefaultFavouriteComponent.Factory,
    private val searchComponentFactory: DefaultSearchComponent.Factory,
    @Assisted componentContext: ComponentContext,
) : RootComponent, ComponentContext by componentContext {

    private fun child(
        config: Config,
        componentContext: ComponentContext,
    ): RootComponent.Child {
        return when (config) {
            is Config.Details -> {
                val component = detailsComponentFactory.create(
                    city = config.city,
                    componentContext = componentContext,
                    onBackClick = {},
                )
                RootComponent.Child.Details(component)
            }

            Config.Favourite -> {
                val component = favouriteComponentFactory.create(
                    componentContext = componentContext,
                    onItemCityClick = { },
                    onAddFavouriteClick = {},
                    onSearchClick = {},
                )
                RootComponent.Child.Favourite(component)
            }

            is Config.Search -> {
                val component = searchComponentFactory.create(
                    componentContext = componentContext,
                    openReason = config.openReason,
                    onBackClick = {},
                    onCitySavedToFavourite = {},
                    onForecastRequested = {},
                )
                RootComponent.Child.Search(component)
            }
        }
    }

    sealed interface Config : Parcelable {

        @Parcelize
        data object Favourite : Config

        @Parcelize
        data class Search(val openReason: OpenReason) : Config

        @Parcelize
        data class Details(val city: City) : Config
    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted componentContext: ComponentContext,
        ): DefaultRootComponent
    }

    override val stack: Value<ChildStack<*, RootComponent.Child>>
        get() = TODO()
}
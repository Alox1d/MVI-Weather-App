package com.sumin.weatherapp.di

import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import dagger.Provides

interface PresentationModule {

    companion object {

        @Provides
        fun provideStoreFactory(): StoreFactory = DefaultStoreFactory()
    }
}
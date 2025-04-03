package com.sumin.weatherapp.di

import javax.inject.Scope

/**
 * Чтобы было понятно, в рамках какого скоупа живёт сущность
 * Используем вместо @Singleton,
 * т.к. по последней непонятно, в рамках активити/фрагмента или всего приложения живёт сущность
 *
 */
@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class ApplicationScope()

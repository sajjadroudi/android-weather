package ir.roudi.weather.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ir.roudi.weather.data.repository.DefaultRepository
import ir.roudi.weather.data.repository.Repository
import ir.roudi.weather.data.local.db.dao.CityDao
import ir.roudi.weather.data.local.db.dao.WeatherDao
import ir.roudi.weather.data.local.pref.DefaultSharedPrefHelper
import ir.roudi.weather.data.local.pref.SharedPrefHelper
import ir.roudi.weather.data.remote.Service
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDefaultSharedPrefHelper(
        @ApplicationContext context: Context
    ) = DefaultSharedPrefHelper(context) as SharedPrefHelper

    @Provides
    @Singleton
    fun provideDefaultRepository(
        cityDao: CityDao,
        weatherDao: WeatherDao,
        service: Service,
        pref: SharedPrefHelper
    ) = DefaultRepository(cityDao, weatherDao, service, pref) as Repository

}
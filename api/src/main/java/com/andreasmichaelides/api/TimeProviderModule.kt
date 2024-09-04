package com.andreasmichaelides.api

import com.andreasmichaelides.api.data.TimeProviderImpl
import com.andreasmichaelides.api.domain.TimeProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class TimeProviderModule {

    @Binds
    abstract fun bindTimeProvider(timeProviderImpl: TimeProviderImpl): TimeProvider

}
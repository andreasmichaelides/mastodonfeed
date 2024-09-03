package com.andreasmichaelides.mastodonfeed

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import kotlinx.coroutines.newSingleThreadContext
import javax.inject.Qualifier
import kotlin.coroutines.CoroutineContext

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ViewModelSingleThreadCoroutineContext

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class LifeSpanInSecondsLong

@Module
@InstallIn(ActivityComponent::class)
object MainActivityModule {

    @ViewModelSingleThreadCoroutineContext
    @Provides
    fun provideRepositoryScope(): CoroutineContext {
        return newSingleThreadContext("ViewModelThread")
    }

    @LifeSpanInSecondsLong
    @Provides
    fun provideLifeSpanInSecondsLong(): Long {
        return 10
    }

}
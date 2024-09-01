package com.andreasmichaelides.api

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MastodonInstanceNameString

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MastodonAccessTokenString

@Module
@InstallIn(SingletonComponent::class)
object MastodonConfigModule {

    @MastodonInstanceNameString
    @Provides
    fun provideMastodonInstanceNameString(): String {
        return "mas.to"
    }

    @MastodonAccessTokenString
    @Provides
    fun provideMastodonAccessTokenString(): String {
        return "FAXrj30FBUM99tCr2LXo63TKZR6M_RFHFiSN2x8H9jM"
    }

    @Provides
    fun provideRepositoryScope(): CoroutineScope {
        return CoroutineScope(Dispatchers.IO)
    }

}
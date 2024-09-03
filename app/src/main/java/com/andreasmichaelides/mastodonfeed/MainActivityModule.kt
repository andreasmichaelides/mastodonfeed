package com.andreasmichaelides.mastodonfeed

import android.content.Context
import android.net.ConnectivityManager
import com.andreasmichaelides.mastodonfeed.data.NetworkConnectivityProviderImpl
import com.andreasmichaelides.mastodonfeed.domain.AndroidNetworkConnectivityMonitor
import com.andreasmichaelides.mastodonfeed.domain.NetworkConnectivityMonitor
import com.andreasmichaelides.mastodonfeed.domain.NetworkConnectivityProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
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
@InstallIn(ViewModelComponent::class)
object MainActivityModule {

    @ViewModelSingleThreadCoroutineContext
    @Provides
    fun provideRepositoryScope(): CoroutineContext {
        return newSingleThreadContext("ViewModelThread")
    }

    /**
     * Change to alter the lifespan of each Feed item
     */
    @LifeSpanInSecondsLong
    @Provides
    fun provideLifeSpanInSecondsLong(): Long {
        return 1 * 60
    }

    @Provides
    fun provideNetworkConnectivityMonitor(networkConnectivityMonitorImpl: AndroidNetworkConnectivityMonitor): NetworkConnectivityMonitor {
        return networkConnectivityMonitorImpl
    }

    @Provides
    fun provideNetworkConnectivityProvider(networkConnectivityProviderImpl: NetworkConnectivityProviderImpl): NetworkConnectivityProvider {
        return networkConnectivityProviderImpl
    }

    @Provides
    fun provideConnectivityManager(@ApplicationContext context: Context): ConnectivityManager {
        return context.getSystemService(ConnectivityManager::class.java) as ConnectivityManager
    }

}
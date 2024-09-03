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
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.newSingleThreadContext
import java.time.Duration
import javax.inject.Qualifier
import kotlin.coroutines.CoroutineContext

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ViewModelSingleThreadCoroutineContext

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class LifeSpanInSecondsLong

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ExpiredFeedsCheckDelayInSecondsDuration

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcherCoroutineContext

@Module
@InstallIn(ViewModelComponent::class)
object MainActivityModule {

    // Creating a single thread coroutineContext, to run our main Viewmodel Flow which handles the inputs, in series, to avoid any
    // race conditions and also to run code separate from the Main thread
    @ViewModelSingleThreadCoroutineContext
    @Provides
    fun provideRepositoryScope(): CoroutineContext {
        return newSingleThreadContext("ViewModelThread")
    }

    @IoDispatcherCoroutineContext
    @Provides
    fun provideIoDispatcherCoroutineContext(): CoroutineContext {
        return Dispatchers.IO
    }

    // Change to alter the lifespan of each Feed item
    @ExpiredFeedsCheckDelayInSecondsDuration
    @Provides
    fun provideExpiredFeedsCheckDelayInSecondsDuration(): Duration {
        return Duration.ofSeconds(1)
    }

    // Change to alter the frequency of expired feeds check
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
package com.andreasmichaelides.mastodonfeed.data

import com.andreasmichaelides.mastodonfeed.domain.NetworkConnectivityMonitor
import com.andreasmichaelides.mastodonfeed.domain.NetworkConnectivityProvider
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NetworkConnectivityProviderImpl @Inject constructor(
    private val networkConnectivityMonitor: NetworkConnectivityMonitor
) : NetworkConnectivityProvider {
    override fun isConnectedToTheInternet(): Flow<Boolean> {
        return networkConnectivityMonitor.isConnectedToTheInternet()
    }
}
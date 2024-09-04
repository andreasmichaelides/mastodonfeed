package com.andreasmichaelides.mastodonfeed.domain

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class AndroidNetworkConnectivityMonitor @Inject constructor(
    private val connectivityManager: ConnectivityManager
) : NetworkConnectivityMonitor {

    private fun createNetworkCallback(isNetworkAvailableFlow: MutableStateFlow<Boolean>) = object : ConnectivityManager.NetworkCallback() {

        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            isNetworkAvailableFlow.update { true }
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            isNetworkAvailableFlow.update { false }
        }
    }

    override fun isConnectedToTheInternet(): Flow<Boolean> {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()

        val isNetworkAvailableFlow = MutableStateFlow(false)
        val networkCallback = createNetworkCallback(isNetworkAvailableFlow)
        connectivityManager.requestNetwork(networkRequest, networkCallback)
        return isNetworkAvailableFlow
    }
}
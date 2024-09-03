package com.andreasmichaelides.mastodonfeed.domain

import kotlinx.coroutines.flow.Flow

interface NetworkConnectivityMonitor {

    fun isConnectedToTheInternet(): Flow<Boolean>

}
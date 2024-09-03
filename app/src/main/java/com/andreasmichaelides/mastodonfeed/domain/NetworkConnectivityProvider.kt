package com.andreasmichaelides.mastodonfeed.domain

import kotlinx.coroutines.flow.Flow

interface NetworkConnectivityProvider {

    fun isConnectedToTheInternet(): Flow<Boolean>

}
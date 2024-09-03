package com.andreasmichaelides.mastodonfeed.domain

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class IsConnectedToTheInternetUseCase @Inject constructor(
    private val networkConnectivityProvider: NetworkConnectivityProvider
) {

    suspend operator fun invoke(): Flow<Boolean> {
        return networkConnectivityProvider.isConnectedToTheInternet()
    }

}
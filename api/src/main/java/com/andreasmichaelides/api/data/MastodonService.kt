package com.andreasmichaelides.api.data

import com.andreasmichaelides.api.domain.StatusItem
import kotlinx.coroutines.flow.Flow

interface MastodonService {

    fun getPublicStream(): Flow<StatusItem>

}
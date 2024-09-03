package com.andreasmichaelides.api.data

import com.andreasmichaelides.api.domain.TimeProvider
import javax.inject.Inject

class TimeProviderImpl @Inject constructor() : TimeProvider {
    override fun getCurrentTimeInMillis(): Long {
        return System.currentTimeMillis()
    }
}
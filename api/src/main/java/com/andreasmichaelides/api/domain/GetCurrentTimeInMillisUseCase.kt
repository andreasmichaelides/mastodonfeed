package com.andreasmichaelides.api.domain

import javax.inject.Inject

class GetCurrentTimeInMillisUseCase @Inject constructor(private val timeProvider: TimeProvider) {

    operator fun invoke(): Long {
        return timeProvider.getCurrentTimeInMillis()
    }

}
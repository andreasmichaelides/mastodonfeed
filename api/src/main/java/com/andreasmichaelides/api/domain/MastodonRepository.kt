package com.andreasmichaelides.api.domain

interface MastodonRepository {

    suspend fun streamFeedItems()

}
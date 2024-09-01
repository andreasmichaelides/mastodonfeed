package com.andreasmichaelides.api.domain

import kotlinx.coroutines.flow.Flow

interface MastodonRepository {

    suspend fun streamFeedItems(): Flow<FeedItem>

}
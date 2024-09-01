package com.andreasmichaelides.api.data

import com.andreasmichaelides.api.domain.FeedItem
import com.andreasmichaelides.api.domain.MastodonRepository
import com.andreasmichaelides.api.domain.mapper.StatusItemToFeedItemMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MastodonRepositoryImpl @Inject constructor(
    private val mastodonService: MastodonService,
    private val statusItemToFeedItemMapper: StatusItemToFeedItemMapper,
    ) : MastodonRepository {

    override suspend fun streamFeedItems(): Flow<FeedItem> {
        return mastodonService.getPublicStream()
            .map { statusItemToFeedItemMapper(it) }
    }
}
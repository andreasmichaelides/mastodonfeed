package com.andreasmichaelides.api.domain

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFeetItemsUseCase @Inject constructor(
    private val mastodonRepository: MastodonRepository
){

    suspend operator fun invoke(): Flow<FeedItem> {
        return mastodonRepository.streamFeedItems()
    }

}
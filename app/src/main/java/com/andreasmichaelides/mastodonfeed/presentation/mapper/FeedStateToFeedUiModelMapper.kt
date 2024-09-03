package com.andreasmichaelides.mastodonfeed.presentation.mapper

import com.andreasmichaelides.mastodonfeed.presentation.FeedState
import com.andreasmichaelides.mastodonfeed.presentation.FeedUiModel
import javax.inject.Inject

class FeedStateToFeedUiModelMapper @Inject constructor(
    private val feedItemToUiFeedItemMapper: FeedItemToUiFeedItemMapper
) {

    operator fun invoke(feedState: FeedState): FeedUiModel {
        return FeedUiModel(
            uiFeedItems = feedState.filteredFeedItems.map { feedItemToUiFeedItemMapper(it) },
            searchFilter = feedState.filter
        )
    }

}
package com.andreasmichaelides.mastodonfeed.presentation.mapper

import com.andreasmichaelides.api.domain.FeedItem
import com.andreasmichaelides.mastodonfeed.presentation.UiFeedItem
import javax.inject.Inject

class FeedItemToUiFeedItemMapper @Inject constructor() {

    operator fun invoke(feedItem: FeedItem): UiFeedItem {
        return UiFeedItem(
            displayName = feedItem.displayName,
            content = feedItem.content,
            avatarUrl = feedItem.avatarUrl,
            userName = feedItem.userName
        )
    }

}